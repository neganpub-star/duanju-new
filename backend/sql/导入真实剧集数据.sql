-- ============================================================
-- 迁移真实短剧数据：duanjua.course_details → duanju
-- 8 部中文剧，保留原始视频/封面 URL
-- 前 3 集免费预览，第 4 集起 VIP 专属
-- ============================================================

USE duanju;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────
-- 1. 清除现有测试假数据（video IDs 1-10）
-- ──────────────────────────────────────────────
DELETE FROM duanju.vs_drama_video_episodes WHERE video_id BETWEEN 1 AND 10;
DELETE FROM duanju.vs_drama_video          WHERE id       BETWEEN 1 AND 10;
ALTER TABLE duanju.vs_drama_video          AUTO_INCREMENT = 1;
ALTER TABLE duanju.vs_drama_video_episodes AUTO_INCREMENT = 1;
DELETE FROM duanju.vs_drama_block          WHERE delete_time IS NULL;

-- ──────────────────────────────────────────────
-- 2. 创建 ID 映射辅助表（source course_id → target video id）
-- ──────────────────────────────────────────────
DROP TEMPORARY TABLE IF EXISTS tmp_course_map;
CREATE TEMPORARY TABLE tmp_course_map (
    target_id   INT PRIMARY KEY,
    course_id   INT,
    category_ids VARCHAR(20),
    description  VARCHAR(200)
);

INSERT INTO tmp_course_map VALUES
(1, 1916, '1,3', '她以为嫁入豪门是幸福开始，却不知暗流涌动……'),
(2, 1990, '1,3', '两个人的爱，一个人的执念，终究敌不过命运的捉弄。'),
(3, 1923, '1,6', '离婚才是她重新做自己的第一步，谁说结局一定是悲剧？'),
(4, 1911, '3,1', '旧日情深化不开，重逢之后，究竟是续情还是放手？'),
(5, 1903, '3',   '倒计时开始，她只想在离别前把所有的爱都给他。'),
(6, 1930, '2',   '朝堂风云，刀光剑影，她以侍女身份潜入，只为复仇。'),
(7, 1907, '4',   '他只想平静生活，奈何这世界非要逼出他真正的实力。'),
(8, 1966, '1,4', '顶级豪门继承人，被宠溺长大，但宠他的人却蓄意已久……');

-- ──────────────────────────────────────────────
-- 3. 插入 8 部剧元数据（封面 URL 直接从源库读取，避免手动转义问题）
-- ──────────────────────────────────────────────
INSERT INTO duanju.vs_drama_video
  (id, site_id, title, image, cover, series_count,
   is_vip, free_episodes, category_ids, status,
   views, description, area, language, source_type,
   weigh, create_time, update_time)
SELECT
  m.target_id,
  1,
  c.title,
  c.title_img,
  c.title_img,
  (SELECT COUNT(*) FROM duanjua.course_details cd WHERE cd.course_id = c.course_id),
  1,   -- is_vip
  3,   -- 前3集免费
  m.category_ids,
  1,
  FLOOR(RAND() * 50000 + 3000),
  m.description,
  '中国大陆', '普通话', 'local',
  (11 - m.target_id),   -- weigh 降序
  NOW(), NOW()
FROM duanjua.course c
JOIN tmp_course_map m ON m.course_id = c.course_id
ORDER BY m.target_id;

-- ──────────────────────────────────────────────
-- 4. 批量插入所有分集（跨库 SELECT，一次性完成）
-- ──────────────────────────────────────────────
INSERT INTO duanju.vs_drama_video_episodes
  (site_id, video_id, title, url, hls_url,
   episode_num, is_free, price, views, status,
   create_time, update_time)
SELECT
  1,
  m.target_id,
  CONCAT('第', cd.episode_number, '集'),
  cd.video_url,
  cd.video_url,
  cd.episode_number,
  IF(cd.episode_number <= 3, 1, 0),   -- 前3集免费
  0.00,                                -- VIP 专属，不单独定价
  FLOOR(RAND() * 5000 + 100),
  1,
  NOW(), NOW()
FROM duanjua.course_details cd
JOIN tmp_course_map m ON m.course_id = cd.course_id
WHERE cd.video_url IS NOT NULL AND cd.video_url != ''
ORDER BY m.target_id, cd.episode_number;

-- ──────────────────────────────────────────────
-- 5. 更新 series_count 为实际入库集数（去掉无效 URL 的集）
-- ──────────────────────────────────────────────
UPDATE duanju.vs_drama_video v
SET v.series_count = (
    SELECT COUNT(*)
    FROM duanju.vs_drama_video_episodes e
    WHERE e.video_id = v.id AND e.delete_time IS NULL
)
WHERE v.delete_time IS NULL AND v.id BETWEEN 1 AND 8;

-- ──────────────────────────────────────────────
-- 6. 首页 focus 轮播图（用真实封面）
-- ──────────────────────────────────────────────
INSERT INTO duanju.vs_drama_block (site_id, name, title, image, url, type, parsetpl, weigh, status, create_time, update_time)
SELECT
  1, c.title, c.title,
  c.title_img,
  CONCAT('/pages/video/play?id=', m.target_id),
  'focus', 1,
  (10 - m.target_id),
  'normal', NOW(), NOW()
FROM duanjua.course c
JOIN tmp_course_map m ON m.course_id = c.course_id
WHERE m.target_id <= 5   -- 取前5部剧做轮播
ORDER BY m.target_id;

DROP TEMPORARY TABLE IF EXISTS tmp_course_map;

SET FOREIGN_KEY_CHECKS = 1;

-- ──────────────────────────────────────────────
-- 验证
-- ──────────────────────────────────────────────
SELECT
  v.id,
  v.title,
  v.series_count,
  v.category_ids,
  v.is_vip,
  ep_total.cnt          AS 入库集数,
  free_ep.cnt           AS 免费集数
FROM duanju.vs_drama_video v
LEFT JOIN (SELECT video_id, COUNT(*) cnt FROM duanju.vs_drama_video_episodes WHERE delete_time IS NULL GROUP BY video_id) ep_total ON ep_total.video_id = v.id
LEFT JOIN (SELECT video_id, COUNT(*) cnt FROM duanju.vs_drama_video_episodes WHERE delete_time IS NULL AND is_free=1 GROUP BY video_id) free_ep  ON free_ep.video_id  = v.id
WHERE v.delete_time IS NULL
ORDER BY v.id;
