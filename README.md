# Daily Ledger / 日账

一个面向个人的每日记账 Web 应用：记一笔、看今天、管住这个月。

> 状态：Step 0 需求与设计进行中；代码骨架将在 Step 1 提交。

## MVP 功能

- 邮箱注册 / 登录，数据按用户隔离
- 记录支出：金额、分类、日期、备注
- 当日支出汇总
- 按月查看流水，支持编辑 / 删除
- 设置月度预算，查看剩余额度
- 月度报告：总支出、预算对比、分类占比、每日趋势、环比

## 技术栈

- 后端：Java 21、Spring Boot 3、MyBatis-Plus、MySQL 8、Redis、Spring 安全 + Session
- 前端：Vue 3、Vite、Vue Router、Bootstrap 5、Chart.js
- 测试：JUnit 5、MockMvc、Testcontainers
- 部署：Docker Compose、Render Free、TiDB Cloud Serverless、Redis Cloud

## 本地开发

> Step 1 完成后补全启动命令。

## 文档

- 产品需求：docs/prd.md（待补）
- 架构决策：docs/adr/（待补）

## License

MIT
