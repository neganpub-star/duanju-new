-- 短剧平台初始化 SQL
-- 数据库: duanju（新建）
-- 说明: 参考原 PHP 系统 vs_ 前缀表重新设计，字段统一使用 datetime 类型

CREATE DATABASE IF NOT EXISTS `duanju` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `duanju`;

-- ============================================================
-- 系统管理员表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_admin` (
    `id`           INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    `username`     VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '用户名',
    `nickname`     VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '昵称',
    `password`     VARCHAR(64)      NOT NULL DEFAULT '' COMMENT 'MD5(password+salt)',
    `salt`         VARCHAR(10)      NOT NULL DEFAULT '' COMMENT '密码盐',
    `avatar`       VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '头像',
    `email`        VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '邮箱',
    `mobile`       VARCHAR(20)      NOT NULL DEFAULT '' COMMENT '手机号',
    `loginfailure` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `logintime`    BIGINT                    DEFAULT NULL COMMENT '最后登录时间戳',
    `loginip`      VARCHAR(50)               DEFAULT NULL COMMENT '最后登录IP',
    `token`        VARCHAR(64)      NOT NULL DEFAULT '' COMMENT 'Session Token',
    `status`       VARCHAR(20)      NOT NULL DEFAULT 'normal' COMMENT 'normal=正常 hidden=禁用',
    `create_time`  DATETIME                  DEFAULT NULL,
    `update_time`  DATETIME                  DEFAULT NULL,
    `delete_time`  DATETIME                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员表';

-- 初始管理员 admin/admin123
INSERT INTO `vs_admin` (`username`, `nickname`, `password`, `salt`, `status`, `create_time`)
VALUES ('admin', '超级管理员', MD5(CONCAT('admin123', 'abc123')), 'abc123', 'normal', NOW())
ON DUPLICATE KEY UPDATE id = id;

-- ============================================================
-- 短剧用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_user` (
    `id`                   BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `site_id`              INT UNSIGNED     NOT NULL DEFAULT 1 COMMENT '站点ID',
    `username`             VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '用户名',
    `password`             VARCHAR(64)      NOT NULL DEFAULT '' COMMENT '密码',
    `salt`                 VARCHAR(10)      NOT NULL DEFAULT '',
    `nickname`             VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`               VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '头像',
    `email`                VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '邮箱',
    `mobile`               VARCHAR(20)      NOT NULL DEFAULT '' COMMENT '手机号',
    `gender`               TINYINT                   DEFAULT 0 COMMENT '0=未知 1=男 2=女',
    `bio`                  VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '个性签名',
    `money`                DECIMAL(10, 2)   NOT NULL DEFAULT 0.00 COMMENT '余额',
    `score`                DECIMAL(10, 2)   NOT NULL DEFAULT 0.00 COMMENT '积分',
    `usable`               DECIMAL(10, 2)   NOT NULL DEFAULT 0.00 COMMENT '可用次数/点数',
    `vip_expire_time`      DATETIME                  DEFAULT NULL COMMENT 'VIP到期时间',
    `reseller_level`       INT              NOT NULL DEFAULT 0 COMMENT '分销等级 0=非分销',
    `reseller_expire_time` DATETIME                  DEFAULT NULL COMMENT '分销到期时间',
    `parent_id`            BIGINT           NOT NULL DEFAULT 0 COMMENT '上级用户ID',
    `login_count`          INT              NOT NULL DEFAULT 0 COMMENT '累计登录次数',
    `login_time`           BIGINT                    DEFAULT NULL COMMENT '最后登录时间戳',
    `login_ip`             VARCHAR(50)               DEFAULT NULL COMMENT '最后登录IP',
    `status`               VARCHAR(20)      NOT NULL DEFAULT 'normal' COMMENT 'normal=正常 hidden=禁用',
    `create_time`          DATETIME                  DEFAULT NULL,
    `update_time`          DATETIME                  DEFAULT NULL,
    `delete_time`          DATETIME                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_site_mobile` (`site_id`, `mobile`),
    KEY `idx_site_status` (`site_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短剧用户表';

-- ============================================================
-- 第三方授权表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_user_oauth` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `site_id`       INT UNSIGNED    NOT NULL DEFAULT 1,
    `user_id`       BIGINT          NOT NULL DEFAULT 0 COMMENT '用户ID',
    `provider`      VARCHAR(50)     NOT NULL COMMENT '厂商: wechat',
    `platform`      VARCHAR(50)     NOT NULL COMMENT '平台: mp/miniapp/app',
    `openid`        VARCHAR(64)     NOT NULL COMMENT '平台openid',
    `unionid`       VARCHAR(64)              DEFAULT NULL COMMENT '微信unionid',
    `nickname`      VARCHAR(100)             DEFAULT '' COMMENT '昵称',
    `sex`           TINYINT                  DEFAULT 0,
    `headimgurl`    VARCHAR(512)             DEFAULT '' COMMENT '头像',
    `session_key`   VARCHAR(64)              DEFAULT NULL,
    `access_token`  VARCHAR(256)             DEFAULT NULL,
    `refresh_token` VARCHAR(256)             DEFAULT NULL,
    `expire_in`     INT                      DEFAULT NULL,
    `expire_time`   BIGINT                   DEFAULT NULL,
    `login_time`    BIGINT                   DEFAULT NULL,
    `login_count`   INT             NOT NULL DEFAULT 0,
    `create_time`   DATETIME                 DEFAULT NULL,
    `update_time`   DATETIME                 DEFAULT NULL,
    `delete_time`   DATETIME                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_openid_platform` (`site_id`, `openid`, `platform`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '第三方授权';

-- ============================================================
-- 短剧主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_video` (
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `site_id`       INT UNSIGNED     NOT NULL DEFAULT 1,
    `title`         VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '短剧名称',
    `image`         VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '封面',
    `cover`         VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '横版封面',
    `series_count`  INT              NOT NULL DEFAULT 0 COMMENT '总集数',
    `is_tv`         TINYINT          NOT NULL DEFAULT 0 COMMENT '0=完结 1=连载',
    `score`         VARCHAR(10)      NOT NULL DEFAULT '0.0' COMMENT '评分',
    `status`        TINYINT          NOT NULL DEFAULT 1 COMMENT '0=隐藏 1=显示',
    `category_ids`  VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '分类ID逗号分隔',
    `tags`          VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '标签',
    `description`   VARCHAR(500)     NOT NULL DEFAULT '' COMMENT '简介',
    `content`       TEXT COMMENT '详细介绍',
    `performer`     VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '主演',
    `director`      VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '导演',
    `area`          VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '地区',
    `year`          VARCHAR(10)      NOT NULL DEFAULT '' COMMENT '年份',
    `language`      VARCHAR(50)      NOT NULL DEFAULT '' COMMENT '语言',
    `views`         INT              NOT NULL DEFAULT 0 COMMENT '真实播放量',
    `fake_views`    INT              NOT NULL DEFAULT 0 COMMENT '虚拟播放量',
    `likes`         INT              NOT NULL DEFAULT 0,
    `fake_likes`    INT              NOT NULL DEFAULT 0,
    `collects`      INT              NOT NULL DEFAULT 0,
    `shares`        INT              NOT NULL DEFAULT 0,
    `comments`      INT              NOT NULL DEFAULT 0,
    `price`         DECIMAL(10, 2)   NOT NULL DEFAULT 0.00 COMMENT '单集解锁价格',
    `is_vip`        TINYINT          NOT NULL DEFAULT 0 COMMENT '0=不需要VIP 1=需要VIP',
    `free_episodes` INT              NOT NULL DEFAULT 0 COMMENT '免费集数',
    `source_type`   VARCHAR(20)      NOT NULL DEFAULT 'local' COMMENT 'local/remote',
    `source_url`    VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '来源URL',
    `weigh`         INT              NOT NULL DEFAULT 0 COMMENT '排序权重',
    `remark`        VARCHAR(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `create_time`   DATETIME                  DEFAULT NULL,
    `update_time`   DATETIME                  DEFAULT NULL,
    `delete_time`   DATETIME                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_site_status` (`site_id`, `status`),
    KEY `idx_weigh` (`weigh`, `id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短剧主表';

-- ============================================================
-- 剧集表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_video_episodes` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `site_id`     INT UNSIGNED    NOT NULL DEFAULT 1,
    `video_id`    BIGINT          NOT NULL COMMENT '关联短剧ID',
    `title`       VARCHAR(100)    NOT NULL DEFAULT '' COMMENT '集标题',
    `duration`    INT             NOT NULL DEFAULT 0 COMMENT '时长（秒）',
    `url`         VARCHAR(512)    NOT NULL DEFAULT '' COMMENT '视频地址',
    `hls_url`     VARCHAR(512)    NOT NULL DEFAULT '' COMMENT 'HLS地址',
    `episode_num` INT             NOT NULL DEFAULT 0 COMMENT '集数',
    `is_free`     TINYINT         NOT NULL DEFAULT 0 COMMENT '0=收费 1=免费',
    `price`       DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '解锁价格',
    `views`       INT             NOT NULL DEFAULT 0,
    `weigh`       INT             NOT NULL DEFAULT 0,
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '0=隐藏 1=显示',
    `create_time` DATETIME                 DEFAULT NULL,
    `update_time` DATETIME                 DEFAULT NULL,
    `delete_time` DATETIME                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_video_id` (`video_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短剧剧集表';

-- ============================================================
-- 分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_category` (
    `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `site_id`     INT UNSIGNED NOT NULL DEFAULT 1,
    `pid`         INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID',
    `name`        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '分类名',
    `type`        VARCHAR(30)  NOT NULL DEFAULT 'video' COMMENT 'video/year/area',
    `style`       TINYINT      NOT NULL DEFAULT 1 COMMENT '层级 1/2/3',
    `image`       VARCHAR(255) NOT NULL DEFAULT '',
    `weigh`       INT          NOT NULL DEFAULT 0,
    `description` VARCHAR(255) NOT NULL DEFAULT '',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'normal',
    `create_time` DATETIME              DEFAULT NULL,
    `update_time` DATETIME              DEFAULT NULL,
    `delete_time` DATETIME              DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pid` (`pid`),
    KEY `idx_site` (`site_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短剧分类';

-- ============================================================
-- 首页区块表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_block` (
    `id`          SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `site_id`     INT UNSIGNED      NOT NULL DEFAULT 1,
    `type`        VARCHAR(20)       NOT NULL DEFAULT 'focus' COMMENT 'focus=焦点图 side=广告图',
    `name`        VARCHAR(50)       NOT NULL DEFAULT '',
    `title`       VARCHAR(100)      NOT NULL DEFAULT '',
    `image`       VARCHAR(255)      NOT NULL DEFAULT '',
    `url`         VARCHAR(255)      NOT NULL DEFAULT '',
    `parsetpl`    TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0=外部 1=内部',
    `weigh`       INT               NOT NULL DEFAULT 0,
    `status`      VARCHAR(20)       NOT NULL DEFAULT 'normal',
    `create_time` DATETIME                   DEFAULT NULL,
    `update_time` DATETIME                   DEFAULT NULL,
    `delete_time` DATETIME                   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '首页区块';

-- ============================================================
-- VIP 套餐
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_vip` (
    `id`             INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    `site_id`        INT UNSIGNED   NOT NULL DEFAULT 1,
    `title`          VARCHAR(50)    NOT NULL COMMENT '套餐名',
    `image`          VARCHAR(255)   NOT NULL DEFAULT '',
    `description`    VARCHAR(255)   NOT NULL DEFAULT '',
    `content`        TEXT COMMENT '权益说明',
    `days`           INT            NOT NULL COMMENT '有效天数',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '划线价',
    `weigh`          INT            NOT NULL DEFAULT 0,
    `status`         VARCHAR(20)    NOT NULL DEFAULT 'normal',
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `delete_time`    DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'VIP套餐';

-- ============================================================
-- VIP 订单
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_vip_order` (
    `id`             BIGINT          NOT NULL AUTO_INCREMENT,
    `site_id`        INT UNSIGNED    NOT NULL DEFAULT 1,
    `vip_id`         INT             NOT NULL DEFAULT 0,
    `order_sn`       VARCHAR(60)     NOT NULL COMMENT '订单号',
    `user_id`        BIGINT          NOT NULL DEFAULT 0,
    `days`           INT             NOT NULL DEFAULT 0 COMMENT '购买天数',
    `status`         TINYINT         NOT NULL DEFAULT 0 COMMENT '-2关闭 -1取消 0未付 1已付 2完成',
    `total_fee`      DECIMAL(10, 2)  NOT NULL,
    `pay_fee`        DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    `transaction_id` VARCHAR(60)              DEFAULT NULL,
    `payment_json`   VARCHAR(2500)            DEFAULT NULL,
    `pay_type`       VARCHAR(20)              DEFAULT NULL COMMENT 'wechat/alipay/wallet/score/cryptocard/system',
    `platform`       VARCHAR(30)              DEFAULT NULL COMMENT 'H5/Web/wxOfficialAccount/wxMiniProgram/App',
    `pay_time`       DATETIME                 DEFAULT NULL,
    `remark`         VARCHAR(255)             DEFAULT NULL,
    `ext`            VARCHAR(2048)            DEFAULT NULL,
    `create_time`    DATETIME                 DEFAULT NULL,
    `update_time`    DATETIME                 DEFAULT NULL,
    `delete_time`    DATETIME                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'VIP订单';

-- ============================================================
-- 积分套餐
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_usable` (
    `id`              INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    `site_id`         INT UNSIGNED   NOT NULL DEFAULT 1,
    `title`           VARCHAR(50)    NOT NULL,
    `image`           VARCHAR(255)   NOT NULL DEFAULT '',
    `flag`            VARCHAR(50)    NOT NULL DEFAULT '',
    `description`     VARCHAR(255)   NOT NULL DEFAULT '',
    `content`         TEXT,
    `usable`          INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '总点数',
    `original_usable` INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '原始点数',
    `give_usable`     INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '赠送点数',
    `price`           DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `give_price`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
    `first_price`     DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '首冲价',
    `original_price`  DECIMAL(10, 2) NOT NULL COMMENT '划线价',
    `status`          VARCHAR(5)     NOT NULL DEFAULT '1' COMMENT '0=不启用 1=启用',
    `weigh`           INT UNSIGNED   NOT NULL DEFAULT 0,
    `create_time`     DATETIME                DEFAULT NULL,
    `update_time`     DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '积分/点数套餐';

-- ============================================================
-- 分销商套餐
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_reseller` (
    `id`             INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    `site_id`        INT UNSIGNED   NOT NULL DEFAULT 1,
    `name`           VARCHAR(50)    NOT NULL,
    `image`          VARCHAR(255)   NOT NULL DEFAULT '',
    `content`        TEXT           NOT NULL,
    `price`          DECIMAL(10, 2) NOT NULL,
    `original_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `level`          TINYINT        NOT NULL COMMENT '分销等级',
    `direct`         DECIMAL(10, 2) NOT NULL COMMENT '直接分润%',
    `indirect`       DECIMAL(10, 2) NOT NULL COMMENT '间接分润%',
    `expire`         INT            NOT NULL COMMENT '有效天数',
    `weigh`          INT            NOT NULL DEFAULT 0,
    `status`         VARCHAR(20)    NOT NULL DEFAULT 'normal',
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `delete_time`    DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分销商套餐';

-- ============================================================
-- 钱包流水
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_user_wallet_log` (
    `id`          BIGINT          NOT NULL AUTO_INCREMENT,
    `site_id`     INT UNSIGNED    NOT NULL DEFAULT 1,
    `user_id`     BIGINT          NOT NULL DEFAULT 0,
    `wallet`      DECIMAL(10, 2)  NOT NULL COMMENT '变动金额（正/负）',
    `wallet_type` VARCHAR(20)     NOT NULL COMMENT 'money/score/usable',
    `type`        VARCHAR(50)     NOT NULL COMMENT '变动类型标识',
    `before`      DECIMAL(10, 2)  NOT NULL COMMENT '变动前',
    `after`       DECIMAL(10, 2)  NOT NULL COMMENT '变动后',
    `item_id`     VARCHAR(60)              DEFAULT NULL COMMENT '关联业务ID',
    `memo`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '备注',
    `create_time` DATETIME                 DEFAULT NULL,
    `update_time` DATETIME                 DEFAULT NULL,
    `delete_time` DATETIME                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_wallet_type` (`wallet_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '钱包流水';

-- ============================================================
-- 提现申请
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_user_wallet_apply` (
    `id`           INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    `site_id`      INT UNSIGNED   NOT NULL DEFAULT 1,
    `user_id`      BIGINT         NOT NULL,
    `apply_sn`     VARCHAR(60)             DEFAULT NULL COMMENT '提现单号',
    `apply_type`   VARCHAR(20)             DEFAULT NULL COMMENT 'bank/wechat/alipay',
    `money`        DECIMAL(10, 2) NOT NULL,
    `actual_money` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `charge_money` DECIMAL(10, 2) NOT NULL COMMENT '手续费',
    `service_fee`  DECIMAL(10, 3)          DEFAULT NULL COMMENT '手续费率',
    `apply_info`   VARCHAR(500)            DEFAULT NULL COMMENT '收款信息JSON',
    `status`       TINYINT                 DEFAULT 0 COMMENT '-1拒绝 0待审 1处理中 2已处理',
    `platform`     VARCHAR(20)             DEFAULT NULL,
    `payment_json` VARCHAR(2500)           DEFAULT NULL,
    `log`          TEXT COMMENT '操作日志',
    `create_time`  DATETIME                DEFAULT NULL,
    `update_time`  DATETIME                DEFAULT NULL,
    UNIQUE KEY `uk_apply_sn` (`apply_sn`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '提现申请';

-- ============================================================
-- 点数套餐订单
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_usable_order` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `site_id`        INT            NOT NULL DEFAULT 1,
    `usable_id`      BIGINT         NOT NULL,
    `order_sn`       VARCHAR(60)    NOT NULL,
    `user_id`        BIGINT         NOT NULL,
    `usable`         INT            NOT NULL DEFAULT 0 COMMENT '点数数量',
    `status`         TINYINT        NOT NULL DEFAULT 0 COMMENT '-1取消 0待支付 1已支付',
    `total_fee`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `pay_fee`        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `transaction_id` VARCHAR(60)             DEFAULT NULL,
    `payment_json`   TEXT,
    `pay_type`       VARCHAR(20)             DEFAULT NULL,
    `platform`       VARCHAR(20)             DEFAULT NULL,
    `pay_time`       DATETIME                DEFAULT NULL,
    `remark`         VARCHAR(200)            DEFAULT NULL,
    `ext`            VARCHAR(500)            DEFAULT NULL,
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `delete_time`    DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '点数套餐订单';

-- ============================================================
-- 分销套餐订单
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_reseller_order` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `site_id`        INT            NOT NULL DEFAULT 1,
    `reseller_id`    BIGINT         NOT NULL,
    `order_sn`       VARCHAR(60)    NOT NULL,
    `user_id`        BIGINT         NOT NULL,
    `times`          INT                     DEFAULT 0,
    `status`         TINYINT        NOT NULL DEFAULT 0 COMMENT '-1取消 0待支付 1已支付',
    `total_fee`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `pay_fee`        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `transaction_id` VARCHAR(60)             DEFAULT NULL,
    `payment_json`   TEXT,
    `pay_type`       VARCHAR(20)             DEFAULT NULL,
    `platform`       VARCHAR(20)             DEFAULT NULL,
    `pay_time`       DATETIME                DEFAULT NULL,
    `remark`         VARCHAR(200)            DEFAULT NULL,
    `ext`            VARCHAR(500)            DEFAULT NULL,
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `delete_time`    DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分销套餐订单';

-- ============================================================
-- 分销绑定关系
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_richtext` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID（1=用户协议 2=隐私协议 3=法律声明 4=联系我们 5=关于我们）',
    `title`       VARCHAR(100) NOT NULL DEFAULT '' COMMENT '标题',
    `content`     MEDIUMTEXT COMMENT '富文本内容（HTML）',
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `delete_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '富文本协议配置';

INSERT IGNORE INTO `vs_drama_richtext` (`id`, `title`, `content`, `create_time`, `update_time`) VALUES
(1, '用户协议', '<p>请在后台"协议管理"中编辑用户协议内容。</p>', NOW(), NOW()),
(2, '隐私协议', '<p>请在后台"协议管理"中编辑隐私协议内容。</p>', NOW(), NOW()),
(3, '法律声明', '<p>请在后台"协议管理"中编辑法律声明内容。</p>', NOW(), NOW()),
(4, '联系我们', '<p>请在后台"协议管理"中编辑联系我们内容。</p>', NOW(), NOW()),
(5, '关于我们', '<p>请在后台"协议管理"中编辑关于我们内容。</p>', NOW(), NOW());

CREATE TABLE IF NOT EXISTS `vs_drama_reseller_bind` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT,
    `site_id`       INT      NOT NULL DEFAULT 1,
    `user_id`       BIGINT   NOT NULL,
    `reseller_id`   BIGINT            DEFAULT NULL,
    `level`         INT               DEFAULT 1,
    `reseller_json` TEXT,
    `expire_time`   DATETIME          DEFAULT NULL,
    `create_time`   DATETIME          DEFAULT NULL,
    `update_time`   DATETIME          DEFAULT NULL,
    `delete_time`   DATETIME          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分销绑定关系';

-- ============================================================
-- 评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_comment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `site_id`        INT          NOT NULL DEFAULT 1,
    `user_id`        BIGINT       NOT NULL,
    `video_id`       BIGINT       NOT NULL,
    `parent_id`      BIGINT       NOT NULL DEFAULT 0 COMMENT '顶级评论为0',
    `reply_user_id`  BIGINT       NOT NULL DEFAULT 0 COMMENT '被回复用户ID，0表示无',
    `reply_nickname` VARCHAR(64)  NOT NULL DEFAULT '',
    `content`        VARCHAR(500) NOT NULL,
    `likes`          INT          NOT NULL DEFAULT 0,
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0隐藏',
    `create_time`    DATETIME     DEFAULT NULL,
    `update_time`    DATETIME     DEFAULT NULL,
    `delete_time`    DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_video_parent` (`video_id`, `parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评论';

-- ============================================================
-- 评论点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vs_drama_comment_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `comment_id`  BIGINT   NOT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `delete_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评论点赞';
