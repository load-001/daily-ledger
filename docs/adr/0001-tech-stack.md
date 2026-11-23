# ADR 0001 · 技术栈选型

- 状态：Accepted
- 日期：2026-11-23
- 决策人：load-001
- 关联：[PRD](../prd.md)、[线框图](../wireframes/README.md)

## 背景

一个人、每周约 35 小时、目标 3 周做出可上线的记账应用，用于投 Java 后端与软件测试岗位。

约束条件：

1. 必须真的上线，成本为 0，不买域名、不绑信用卡。
2. 范围只保留 6 个用户故事（PRD 第 3 节）。
3. 技术栈要覆盖招聘 JD 里的关键词：Spring Boot、MyBatis-Plus、MySQL、Redis。
4. 电脑和手机都要能用。
5. 只有我一个人，没有运维，不能承担需要 7×24 值守的方案。

下面六个决策按“影响面从大到小”排列。每节固定四段：背景、候选方案、决定、代价。**代价一段是这份文档最重要的部分**——只有优点的选型说明等于没做选型。

---

## 决策 1：后端用 Spring Boot 3 + Java 21

### 背景

后端需要：HTTP 接口、参数校验、鉴权、数据库事务、健康检查、测试支持。这些如果自己拼，工作量会吃掉整个项目的时间。

### 候选方案

- **Servlet + Tomcat 手写**：最贴近底层，能讲清 HTTP 生命周期；但把 JSON、校验、异常处理、连接池拼起来就要一周，且没有额外加分。否决。
- **Spring Boot 3**：自动配置 + 起步依赖，Security / Validation / Actuator / Test 一体。
- **Quarkus / Micronaut**：启动快、内存小；但招聘市场上岗位少，对求职不划算。否决。
- **Node / Go / Python**：更轻；与目标岗位（Java 后端）不匹配。否决。

### 决定

Spring Boot 3.x + Java 21。

1. **岗位匹配**：JD 里写的就是 Spring Boot + MyBatis-Plus，用别的栈等于把最好的证据丢掉。
2. **省掉胶水代码**：五个 starter 覆盖 80% 的基础设施，省下的时间用在业务和测试上。
3. **Java 21 是 LTS**，升级路径干净。
4. **资料密度高**：出问题能搜到答案，一个人做项目这点很关键。

### 代价

1. **启动慢、内存高**：空应用常驻 150–250 MB。Render 免费实例只有 512 MB，必须压 JVM：```-Xmx256m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC```。
2. **抽象层厚，容易只懂注解不懂原理**：```@Transactional``` 失效的六种场景、```@Cacheable``` 自调用失效，都是“会用但不懂”的典型。应对方式是把每个注解背后的机制写进文档。
3. **Boot 3 的 Jakarta 迁移**：网上大量 Boot 2 代码是 ```javax.*```，直接抄会编译不过，搜索要带 ```jakarta``` 关键词。
4. **版本耦合**：Spring Boot 3.2 与 MyBatis-Plus 的兼容窗口有限，必须锁版本。

---

## 决策 2：持久层用 MyBatis-Plus，不用 JPA

### 背景

本项目 SQL 分两类：

- **CRUD**：插入支出、按 id 查、按 id 软删除。结构固定、重复度高。
- **聚合**：今日按分类小计、某月总额、每日趋势（```GROUP BY DATE(expense_date)```）、分类占比、最大支出日。这些是报表主体。

### 候选方案

- **JPA / Hibernate**：实体状态管理、自动 DDL、跨库。但本项目以聚合为主，脏检查、一级缓存没收益；复杂聚合要写 JPQL 或 native SQL，等于放弃它最大的好处却承担它的复杂度。否决。
- **原生 MyBatis**：SQL 完全可控、动态 SQL 强；但 4 张表 × 5 个方法的 CRUD 全要手写，约 20 段重复 SQL。否决。
- **JdbcTemplate**：最直接；手写 RowMapper 和分页，样板代码最多。否决。
- **MyBatis-Plus**：```BaseMapper``` 免写 CRUD、```LambdaQueryWrapper``` 类型安全、分页插件、逻辑删除、字段自动填充。

### 决定

MyBatis-Plus 3.5.x，SQL 显式书写。

1. **SQL 就是文档**：面试时能直接贴出月报那条 SQL 讲索引和执行计划，比“Hibernate 帮我生成了 SQL”有力得多。
2. **```BaseMapper<T>``` 消掉样板**：```insert``` / ```selectById``` / ```updateById``` / ```deleteById``` 全部自带。
3. **逻辑删除内建**：```@TableLogic``` + 全局配置，```BaseMapper``` 的查询自动带 ```deleted_at IS NULL```。
4. **分页插件**：```PaginationInnerInterceptor``` 自动改写 SQL 加 ```LIMIT```。

### 代价

1. **逻辑删除的全局性是双刃剑**：```BaseMapper``` 会自动过滤，但我手写的聚合 SQL **不会**——统计里必须显式写 ```AND deleted_at IS NULL```。漏一次，报表就把已删除的支出算进去。这是我把它写进 ADR 而不只写进代码注释的原因。
2. **```Wrapper``` 拼复杂查询会变丑**：多表 join、子查询表达力不足，最后仍要落回 XML。项目约定：**单表条件查询用 Wrapper，聚合与多表用 XML**。
3. **分页插件的 count 有坑**：默认再发一条 ```SELECT COUNT(*)```，遇到 ```GROUP BY``` 时语义错误，要自定义 count SQL 或干脆手写两条。
4. **多一层依赖**：MyBatis-Plus、MyBatis、Spring Boot 三方版本要对齐。

### 如果重来

聚合报表从第一天就用 XML 写，而不是先试 Wrapper 再改。

---

## 决策 3：MySQL 存业务，Redis 存会话与热点统计

### 背景

数据分两类：**必须持久且强一致**的（用户、支出、预算），和**可重建、读多写少**的（今日汇总、月度统计、Session、登录失败次数）。

### 决定

| 数据 | 存放 | 理由 |
|---|---|---|
| users / categories / expenses / budgets | MySQL 8 | 需要事务、唯一约束、金额精度 |
| 金额 | ```DECIMAL(12,2)``` | ```DOUBLE``` 会在 ```0.1 + 0.2``` 上出问题；Java 侧用 ```BigDecimal``` |
| Session | Redis | 应用重启不掉登录；未来多实例可共享 |
| 登录失败计数 | Redis，```login:fail:{email}:{ip}```，TTL 15 分钟 | 天然过期，不污染业务库 |
| 今日汇总 / 月度统计 / 预算 | Redis，Cache-Aside | 首页每次跑 3 条聚合 SQL，缓存后降到 1 次读 |

缓存规则（这是设计，不是实现细节）：

1. **Cache-Aside**：读 miss 才回源；写完**删缓存**而不是更新缓存——并发写时更新会写出旧值，删除最多多一次回源。
2. **事务提交后删**：用 ```TransactionSynchronization#afterCommit```，否则事务回滚了缓存却被改，留下脏数据。
3. **key 带用户和日期**：```stats:daily:{userId}:{date}```、```stats:monthly:{userId}:{yyyy-MM}```。不带 userId 会串号，这是最典型的多租户事故。
4. **改日期要删 4 个 key**：旧日期、新日期、旧月份、新月份。**跨月改日期只删新月份是真实会发生的 bug**。
5. **TTL 兜底**：删除逻辑漏了，过期也能自愈。

### 代价

1. **一致性窗口**：删缓存到回源之间读到旧值。个人记账可容忍秒级延迟，代价是预算校验不能做强一致——所以“超支”只提示、不拦截。
2. **缓存击穿**：热门 key 过期瞬间打穿到 MySQL。免费实例 0.1 CPU，一次击穿就能卡住页面。应对：TTL 加随机抖动、空值也缓存短 TTL。
3. **多一个故障点**：Redis 挂了 Session 一起挂，等于全站登出。必须接受，并监控。
4. **额度限制**：Redis Cloud 免费版 30 MB、100 ops/s、30 连接。缓存必须设 TTL，不能把全量流水塞进去。

### 真实踩坑（实现阶段填写）

> 待补：跨月修改支出日期后月报金额对不上，根因是只失效了新月份的 key，旧月份仍是旧值。修复方式：失效清单改为「旧日期 / 新日期 / 旧月份 / 新月份」，并补一条集成测试固定这个行为。

---

## 决策 4：前端用 Vue 3 + Vite + SFC，不用 Thymeleaf 服务端渲染

### 背景

4 个页面：登录、首页、流水、月报。首页有即时反馈（记一笔后今日汇总立刻变），月报有图表。

### 候选方案

- **Thymeleaf + HTMX**：一个进程、无 Node、部署最简单；但交互要围绕 HTML 片段设计，图表和表单联动写起来别扭。否决。
- **无构建 Vue（本地 vendor）**：有组件化、无构建链；但单文件组织差、没有 HMR。否决。
- **Vue 3 + Vite + SFC**：组件化、HMR、生态成熟、简历关键词。
- **React**：生态更大；与既有经验不匹配，学习成本吃掉时间。否决。

### 决定

Vue 3 + Vite + SFC + Vue Router，JavaScript + ```<script setup>```；Bootstrap 5 + Chart.js。**不引入** Pinia、Element Plus、TypeScript、SSR。

1. 交互确实是客户端的：记一笔后局部刷新、月份切换、图表重绘，用 SPA 更自然。
2. 我熟悉 Vue，能把时间花在后端和测试上——这才是项目重点。
3. 明确划界：Vue 只负责渲染和请求，**业务规则（校验、权限、预算计算）全在后端**，前端不做第二套逻辑。

### 代价

1. **多一条构建链**：Node 22 + npm，```package-lock.json``` 必须提交，CI 用 ```npm ci```；Dockerfile 变三段（Node 构建 → Maven 打包 → JRE 运行）。
2. **首屏白屏**：SPA 要先下 JS 再渲染。Render 冷启动约 1 分钟会放大这个等待，必须有 loading 态。
3. **构建产物怎么进 jar**：用 ```maven-resources-plugin``` 把 ```frontend/dist``` 复制到 ```target/classes/static```，而不是把 Node 塞进 Maven 生命周期——后者会让 ```mvn``` 依赖 Node，构建环境变脆。
4. **后端仍要放行静态资源**：```/```、```/index.html```、```/expenses```、```/reports```、```/assets/**``` 必须匿名可访问，```/api/**``` 仍要认证。配错会出现“页面能开但接口 401”或“页面也被拦”。

### 如果重来

如果目标是“最快上线”，Thymeleaf + HTMX 能省 1–2 天；既然选了 SPA，就要接受成本，不能中途再改。

---

## 决策 5：认证用 Session + Redis，不用 JWT

### 背景

单应用、同源部署（前端和 API 同一域名）、需要“退出登录立刻生效”。

### 候选方案

- **JWT（无状态）**：服务端不存状态、天然适合多端；但无法主动失效，要额外做刷新 token 和黑名单，仍要防 CSRF。否决。
- **Session + Redis**：可随时踢人、实现简单、Spring Security 原生支持。
- **Session 存内存**：零依赖；但重启掉线、无法多实例。否决。

### 决定

Spring Security 表单 + Session，Session 存 Redis。

1. **可撤销性**：改密码、怀疑被盗、管理员踢人，删一个 key 就生效。JWT 要做到这点必须引入黑名单，而黑名单本身就是“有状态”，等于抵消了 JWT 的优点。
2. **同源部署**：前端和 API 同域名，Cookie 自动带上，不用处理跨域，也不用把 token 放 localStorage（XSS 风险）。
3. **CSRF 本来就要防**：```CookieCsrfTokenRepository``` + ```X-XSRF-TOKEN``` 是标准做法。
4. **简化前端**：不用写 token 刷新和拦截器重试。

### 代价

1. **依赖 Redis 可用性**：Redis 挂则全员掉线。可接受，但要监控。
2. **跨端不适用**：做小程序或原生 App 时没有 Cookie 环境，必须换 JWT 或 OAuth2。**这是“当前场景正确”而非“永远正确”的决策。**
3. **要配 Cookie 属性**：```HttpOnly```、```SameSite=Lax```，生产 HTTPS 下加 ```Secure```。
4. **Session 序列化**：存 Redis 的对象要实现 ```Serializable```，类结构变更会导致旧 Session 反序列化失败——发布时需要用户重新登录，要写进发布说明。

---

## 决策 6：本地 Docker Compose，线上 Render Free + TiDB Cloud + Redis Cloud

### 背景

硬约束：不买域名、不绑信用卡、不能保证电脑一直开着。

### 候选方案

- **自建 VPS（Oracle Free 等）**：0 元但需信用卡验证、需自己运维安全组/防火墙/证书/备份。否决。
- **Cloudflare / Tailscale 隧道**：0 元、免卡；但必须本地电脑开机，不满足“不开电脑”。否决。
- **GitHub Pages**：0 元；跑不了 Spring Boot、MySQL、Redis。否决。
- **Render Free + 外部 MySQL/Redis**：0 元、免卡。

### 决定

- 本地：```docker compose``` 起 MySQL 8 + Redis 7，版本与线上对齐。
- 线上：Render Free Web Service（Docker 部署、自动 HTTPS）+ TiDB Cloud Serverless（MySQL 兼容，5 GiB）+ Redis Cloud Free（30 MB）。

1. **零成本、零绑卡**，满足硬约束。
2. **Push 即部署**：GitHub 直连，```render.yaml``` 声明式配置，不需要登录服务器。
3. **本地与线上依赖一致**：Compose 锁 MySQL 8.0.x 与 Redis 7.x。

### 代价

1. **Render Free 的硬限制**：512 MB 内存、0.1 CPU、15 分钟无流量休眠、冷启动约 1 分钟、750 实例小时/月。不能压测，JMeter 只能跑本地。
2. **TiDB 不是 Oracle MySQL**：MySQL 兼容但有行为差异，**SQL 正确性以本地 MySQL 8 为准**，线上只做验证。强制 TLS，JDBC 要 ```sslMode=REQUIRED```。
3. **Redis Cloud 30 MB 且无复制**：不能当唯一数据源，只能放缓存和 Session。
4. **免费政策会变**：Aiven 免费 MySQL 已停止新建就是例子。代码里不能写死平台特性。
5. **无自动备份**：要自己定期 ```mysqldump``` 到本地，这也是“数据库备份”这条铁律的落地证据。
6. **真实公司里对应什么**：K8s/ECS + RDS + ElastiCache + 对象存储 + CDN。写清这点，面试时才不会被问倒。

---

## 被否决的方案汇总

| 技术 | 否决理由 |
|---|---|
| JWT + Refresh Token | 单应用同源场景下复杂度收益比不划算 |
| 微服务 / 消息队列 | 一个用户、一条流程，拆开只增加部署和调试成本 |
| Elasticsearch | 数据量最多几万条，MySQL 索引足够 |
| Pinia | 4 个页面的状态用不上全局 store |
| TypeScript | 这个体量下类型收益抵不过学习成本 |
| Element Plus | 组件库让包体翻倍，Bootstrap 5 够用 |
| Swagger / SpringDoc | 10 个接口、前端自己写、测试由 MockMvc 覆盖，不解决实际问题 |
| K8s | 单实例部署，编排能力用不上 |

## 后续复审触发条件

1. 需要支持多端（小程序 / App）→ 决策 5 改为 JWT + OAuth2。
2. 单用户数据量超过 10 万条、报表变慢 → 决策 3 引入预聚合表。
3. 需要多实例部署 → 决策 3 的缓存一致性策略要升级。
4. 免费额度不可用 → 决策 6 切换到自建 VPS 或付费实例。
