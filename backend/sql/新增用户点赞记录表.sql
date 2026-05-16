-- 用户点赞记录表（防止重复点赞）
CREATE TABLE IF NOT EXISTS `vs_drama_video_like` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `site_id`     INT UNSIGNED    NOT NULL DEFAULT 1,
    `user_id`     BIGINT UNSIGNED NOT NULL,
    `video_id`    BIGINT UNSIGNED NOT NULL,
    `create_time` DATETIME        DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    KEY `idx_video_id` (`video_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户点赞记录';
