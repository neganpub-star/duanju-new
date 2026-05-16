-- 新增转码配置到系统参数表
-- 执行时间：2026-05-16

INSERT INTO sys_config (config_key, config_value, config_name, config_type, config_group, remark) VALUES
('transcode.provider',                 'ffmpeg',          '转码提供商',         'text', 'transcode', 'ffmpeg / aliyun'),
('transcode.ffmpeg.bin',               'ffmpeg',          'FFmpeg 可执行路径',   'text', 'transcode', '已加入 PATH 则填 ffmpeg，否则填完整路径如 /usr/local/bin/ffmpeg'),
('transcode.ffmpeg.output-dir',        '/data/videos',    '转码输出目录',        'text', 'transcode', '服务器本地目录，需有可写权限'),
('transcode.ffmpeg.output-url-prefix', 'http://localhost/videos', '转码访问 URL 前缀', 'text', 'transcode', '转码切片文件对外访问的域名+路径前缀'),
('transcode.ffmpeg.qualities',
 '[{"definition":"360p","scale":"640:360","bitrate":"800k"},{"definition":"720p","scale":"1280:720","bitrate":"2000k"},{"definition":"1080p","scale":"1920:1080","bitrate":"4000k"}]',
 '转码清晰度配置', 'json', 'transcode', 'JSON 数组，每项包含 definition / scale / bitrate'),

-- 阿里云 VOD 配置
('transcode.aliyun.access-key-id',     '', '阿里云 AccessKeyId',     'text',     'transcode', '阿里云账号 AccessKeyId'),
('transcode.aliyun.access-key-secret', '', '阿里云 AccessKeySecret', 'password', 'transcode', '阿里云账号 AccessKeySecret'),
('transcode.aliyun.region-id',         'cn-shanghai', '阿里云 VOD 区域',     'text', 'transcode', '如 cn-shanghai / cn-beijing'),
('transcode.aliyun.template-group-id', '', '转码模板组 ID', 'text', 'transcode', '阿里云 VOD 控制台 → 配置管理 → 转码设置 → 模板组 ID');
