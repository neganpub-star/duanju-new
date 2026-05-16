-- 分集表新增转码相关字段
-- 执行时间：2026-05-15

ALTER TABLE vs_drama_video_episodes
  ADD COLUMN play_info        JSON          DEFAULT NULL COMMENT '多清晰度播放信息 JSON数组，格式：[{"definition":"720p","url":"..."},...]',
  ADD COLUMN transcode_status VARCHAR(20)   DEFAULT NULL COMMENT '转码状态：pending/processing/done/failed',
  ADD COLUMN transcode_msg    VARCHAR(500)  DEFAULT NULL COMMENT '转码失败原因';
