-- V1: 初始表结构
CREATE TABLE `users` (
                       `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                       `email`         VARCHAR(120) NOT NULL,
                       `password_hash` VARCHAR(100) NOT NULL,
                       `display_name`  VARCHAR(50)  NOT NULL DEFAULT '',
                       `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (`id`),
                       UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';

CREATE TABLE `categories` (
                            `id`         BIGINT      NOT NULL AUTO_INCREMENT,
                            `user_id`    BIGINT      NOT NULL DEFAULT 0 COMMENT '0 表示系统公共分类',
                            `name`       VARCHAR(30) NOT NULL,
                            `icon`       VARCHAR(20) NOT NULL DEFAULT '',
                            `sort_order` INT         NOT NULL DEFAULT 0,
                            `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_categories_user_name` (`user_id`, `name`),
                            KEY `idx_categories_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出分类';

CREATE TABLE `expenses` (
                          `id`           BIGINT        NOT NULL AUTO_INCREMENT,
                          `user_id`      BIGINT        NOT NULL,
                          `category_id`  BIGINT        NOT NULL,
                          `amount`       DECIMAL(12,2) NOT NULL COMMENT '金额，单位元',
                          `expense_date` DATE          NOT NULL COMMENT '消费日期',
                          `note`         VARCHAR(200)  NOT NULL DEFAULT '',
                          `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `deleted_at`   DATETIME      NULL DEFAULT NULL COMMENT '软删除时间',
                          PRIMARY KEY (`id`),
                          KEY `idx_expenses_user_date` (`user_id`, `expense_date`),
                          KEY `idx_expenses_user_category_date` (`user_id`, `category_id`, `expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出流水';

CREATE TABLE `budgets` (
                         `id`           BIGINT        NOT NULL AUTO_INCREMENT,
                         `user_id`      BIGINT        NOT NULL,
                         `budget_month` DATE          NOT NULL COMMENT '所在月 1 号',
                         `amount`       DECIMAL(12,2) NOT NULL,
                         `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_budgets_user_month` (`user_id`, `budget_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='月度预算';

INSERT INTO `categories` (`user_id`, `name`, `icon`, `sort_order`) VALUES
                                                                     (0, '餐饮', 'utensils', 10),
                                                                     (0, '交通', 'bus',      20),
                                                                     (0, '购物', 'bag',      30),
                                                                     (0, '居住', 'home',     40),
                                                                     (0, '娱乐', 'gamepad',  50),
                                                                     (0, '医疗', 'heart',    60),
                                                                     (0, '其他', 'ellipsis', 90);
