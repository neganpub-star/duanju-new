-- 创建数据库
CREATE DATABASE IF NOT EXISTS commentdb DEFAULT CHARSET utf8mb4;
USE commentdb;

-- 创建评论表
CREATE TABLE IF NOT EXISTS `comments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `video_id` VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `user_nick` VARCHAR(64),
  `content` TEXT NOT NULL,
  `parent_id` INT DEFAULT NULL,
  `create_time` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 