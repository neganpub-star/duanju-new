# 通用视频评论后端（MySQL版）

## 快速启动

1. 安装依赖

   ```bash
   npm install
   ```

2. 配置数据库连接

   编辑 `config.js`，填写你的MySQL数据库地址、用户名、密码、数据库名。

3. 创建数据库和表

   ```sql
   CREATE DATABASE IF NOT EXISTS commentdb DEFAULT CHARSET utf8mb4;
   USE commentdb;
   CREATE TABLE `comments` (
     `id` INT AUTO_INCREMENT PRIMARY KEY,
     `video_id` VARCHAR(64) NOT NULL,
     `user_id` VARCHAR(64) NOT NULL,
     `user_nick` VARCHAR(64),
     `content` TEXT NOT NULL,
     `parent_id` INT DEFAULT NULL,
     `create_time` INT NOT NULL
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
   ```

4. 启动服务

   ```bash
   npm start
   ```

   默认监听 http://localhost:3000

## API接口

- `GET /api/commentList?video_id=xxx` 获取评论列表
- `POST /api/addComment` 添加评论
- `POST /api/deleteComment` 删除评论

## 云平台部署

- 支持Railway、Render、阿里云、腾讯云等平台
- 只需上传本项目，配置好MySQL数据库和config.js即可

## 本地MongoDB安装

- [MongoDB下载](https://www.mongodb.com/try/download/community)
- 或用 [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register) 免费云数据库 