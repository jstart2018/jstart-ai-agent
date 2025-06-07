create database if not exists `jstart_ai_agent` default charset utf8mb4 collate utf8mb4_unicode_ci;

use `jstart_ai_agent`;

CREATE TABLE IF NOT EXISTS `conversation_memory`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(64)     NOT NULL comment '会话id',
    `message_type`            varchar(10)     not null comment '消息类型',
    `memory`          text            NOT NULL comment '消息记忆内容',
    `created_at`      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete`       tinyint                  default 0,
    PRIMARY KEY (`id`),
    INDEX idx_conv_prefix (conversation_id(10))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
