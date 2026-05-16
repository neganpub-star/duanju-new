-- 短剧主表新增推荐字段
ALTER TABLE vs_drama_video
    ADD COLUMN `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐到发现页 0=否 1=是' AFTER `status`;

ALTER TABLE vs_drama_video
    ADD INDEX `idx_recommend` (`is_recommend`);
