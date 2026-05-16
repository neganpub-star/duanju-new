-- 新增平台配置和通用配置
-- 执行时间：2026-05-16

INSERT INTO sys_config (config_key, config_value, config_name, config_type, config_group, remark) VALUES
-- 微信相关
('platform.wechat.appid',              'wxb9d65b425e3db047', '微信小程序 AppID',          'text',     'platform', '微信公众平台申请的小程序 AppID'),
('platform.wechat.ios.universal-links','',                   '微信支付 iOS Universal Links','text',    'platform', '微信开放平台配置，iOS 微信支付必须填写'),
-- 抖音小程序
('platform.toutiao.appid',             'ttd5aaa3174aa04dcb01','抖音小程序 AppID',          'text',     'platform', '字节跳动开发者平台申请的小程序 AppID'),
-- 快手小程序
('platform.kuaishou.appid',            'ks648799825228900750','快手小程序 AppID',          'text',     'platform', '快手开放平台申请的小程序 AppID'),
-- App 下载与更新
('platform.app.android.download-url',  '',                   'Android 下载地址',           'text',     'platform', 'Android APK 下载链接'),
('platform.app.ios.download-url',      '',                   'iOS 下载地址',               'text',     'platform', 'App Store 应用链接'),
('platform.app.force-update-version',  '',                   '强制更新版本号',              'text',     'platform', '低于此版本强制更新，留空不强制'),
-- 通用配置（管理员可在后台自由增删）示例
('general.site.name',  '短剧平台',    '站点名称',   'text', 'general', '显示在页面标题、分享等位置'),
('general.site.logo',  '',            '站点 Logo',  'text', 'general', '完整 URL，建议上传到 OSS'),
('general.contact.qq', '',            '客服 QQ',    'text', 'general', '用于用户端展示');
