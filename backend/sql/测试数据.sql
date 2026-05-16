-- 短剧平台测试数据
-- 执行前确保已执行 init.sql
USE `duanju`;

-- ============================================================
-- 1. 分类数据
-- ============================================================
INSERT INTO `vs_drama_category` (`site_id`, `pid`, `name`, `type`, `style`, `weigh`, `status`, `create_time`) VALUES
(1, 0, '都市爱情', 'video', 1, 100, 'normal', NOW()),
(1, 0, '古装宫廷', 'video', 1, 90,  'normal', NOW()),
(1, 0, '甜宠虐恋', 'video', 1, 80,  'normal', NOW()),
(1, 0, '逆袭爽剧', 'video', 1, 70,  'normal', NOW()),
(1, 0, '悬疑推理', 'video', 1, 60,  'normal', NOW()),
(1, 0, '家庭伦理', 'video', 1, 50,  'normal', NOW()),
(1, 0, '2024', 'year', 2, 100, 'normal', NOW()),
(1, 0, '2023', 'year', 2, 90,  'normal', NOW()),
(1, 0, '国产', 'area', 3, 100, 'normal', NOW()),
(1, 0, '港台', 'area', 3, 80,  'normal', NOW());

-- ============================================================
-- 2. 用户数据（10个，含不同状态和钱包余额）
-- ============================================================
INSERT INTO `vs_drama_user`
  (`site_id`, `username`, `password`, `salt`, `nickname`, `avatar`, `mobile`, `gender`,
   `money`, `score`, `usable`, `vip_expire_time`, `reseller_level`, `reseller_expire_time`,
   `parent_id`, `login_count`, `login_time`, `status`, `create_time`)
VALUES
(1, 'user_zhang', MD5(CONCAT('Pass1234','s1')), 's1', '张小明', 'https://example.com/av1.jpg', '13800000001', 1,
 128.50, 300.00, 50.00, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NULL, 0, 12, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_li', MD5(CONCAT('Pass1234','s2')), 's2', '李晓红', 'https://example.com/av2.jpg', '13800000002', 2,
 0.00, 0.00, 10.00, NULL, 0, NULL, 1, 5, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_wang', MD5(CONCAT('Pass1234','s3')), 's3', '王大力', 'https://example.com/av3.jpg', '13800000003', 1,
 500.00, 1200.00, 200.00, DATE_ADD(NOW(), INTERVAL 365 DAY), 1, DATE_ADD(NOW(), INTERVAL 180 DAY), 0, 88, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_zhao', MD5(CONCAT('Pass1234','s4')), 's4', '赵梦琪', 'https://example.com/av4.jpg', '13800000004', 2,
 88.00, 500.00, 0.00, DATE_ADD(NOW(), INTERVAL 7 DAY), 0, NULL, 1, 20, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_chen', MD5(CONCAT('Pass1234','s5')), 's5', '陈志远', 'https://example.com/av5.jpg', '13800000005', 1,
 1200.00, 3000.00, 500.00, DATE_ADD(NOW(), INTERVAL 365 DAY), 2, DATE_ADD(NOW(), INTERVAL 365 DAY), 0, 200, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_liu', MD5(CONCAT('Pass1234','s6')), 's6', '刘思思', 'https://example.com/av6.jpg', '13800000006', 2,
 0.00, 100.00, 5.00, NULL, 0, NULL, 3, 3, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_wu', MD5(CONCAT('Pass1234','s7')), 's7', '吴俊豪', 'https://example.com/av7.jpg', '13800000007', 1,
 300.00, 800.00, 100.00, DATE_ADD(NOW(), INTERVAL 60 DAY), 1, DATE_ADD(NOW(), INTERVAL 90 DAY), 3, 45, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_sun', MD5(CONCAT('Pass1234','s8')), 's8', '孙丽娜', 'https://example.com/av8.jpg', '13800000008', 2,
 50.00, 200.00, 0.00, DATE_ADD(NOW(), INTERVAL 15 DAY), 0, NULL, 5, 8, UNIX_TIMESTAMP(), 'normal', NOW()),

(1, 'user_zhou', MD5(CONCAT('Pass1234','s9')), 's9', '周宇飞', 'https://example.com/av9.jpg', '13800000009', 1,
 0.00, 0.00, 0.00, NULL, 0, NULL, 0, 1, UNIX_TIMESTAMP(), 'hidden', NOW()),

(1, 'user_xu', MD5(CONCAT('Pass1234','sa')), 'sa', '徐曼妮', 'https://example.com/av10.jpg', '13800000010', 2,
 60.00, 150.00, 20.00, DATE_ADD(NOW(), INTERVAL 90 DAY), 0, NULL, 5, 30, UNIX_TIMESTAMP(), 'normal', NOW());

-- ============================================================
-- 3. 短剧主表数据（8部剧）
-- ============================================================
INSERT INTO `vs_drama_video`
  (`site_id`, `title`, `image`, `cover`, `series_count`, `is_tv`, `score`, `status`,
   `category_ids`, `tags`, `description`, `performer`, `director`, `area`, `year`, `language`,
   `views`, `fake_views`, `likes`, `fake_likes`, `price`, `is_vip`, `free_episodes`,
   `source_type`, `weigh`, `create_time`)
VALUES
(1, '总裁的独家宠爱', 'https://img.example.com/v1.jpg', 'https://img.example.com/v1c.jpg',
 80, 0, '9.2', 1, '1,3', '甜宠,总裁,都市', '平凡女孩意外成为亿万总裁的妻子，开启甜蜜宠溺之旅。',
 '林晓燕,顾北辰', '王导演', '国产', '2024', '普通话',
 15000, 50000, 3200, 8000, 1.00, 1, 5, 'local', 100, NOW()),

(1, '凤凰涅槃：皇后的逆袭', 'https://img.example.com/v2.jpg', 'https://img.example.com/v2c.jpg',
 60, 0, '9.5', 1, '2,3', '古装,宫斗,逆袭', '被打入冷宫的皇后涅槃重生，步步为营夺回一切。',
 '白若曦,萧慕言', '李导演', '国产', '2024', '普通话',
 28000, 80000, 6500, 12000, 1.50, 1, 3, 'local', 95, NOW()),

(1, '萌宝驾到：爸爸你好', 'https://img.example.com/v3.jpg', 'https://img.example.com/v3c.jpg',
 50, 0, '9.0', 1, '1', '萌宝,都市,甜宠', '失忆总裁意外找到五岁萌宝，父女情深温馨日常。',
 '苏糖糖,沈晟', '张导演', '国产', '2024', '普通话',
 9000, 30000, 2100, 5000, 1.00, 0, 10, 'local', 88, NOW()),

(1, '废柴学霸的翻身仗', 'https://img.example.com/v4.jpg', 'https://img.example.com/v4c.jpg',
 40, 1, '8.8', 1, '4', '逆袭,校园,爽剧', '被嘲笑的废柴觉醒隐藏技能，一路打脸成为人生赢家。',
 '陈浩南,方晓晓', '刘导演', '国产', '2024', '普通话',
 5000, 20000, 1500, 4000, 0.50, 0, 8, 'local', 80, NOW()),

(1, '暗夜猎手', 'https://img.example.com/v5.jpg', 'https://img.example.com/v5c.jpg',
 35, 0, '9.3', 1, '5', '悬疑,犯罪,推理', '神秘侦探追踪连环谋杀案，真相令人不寒而栗。',
 '莫寒,江若', '陈导演', '国产', '2024', '普通话',
 12000, 35000, 4000, 7000, 2.00, 1, 2, 'local', 75, NOW()),

(1, '妈妈的心愿', 'https://img.example.com/v6.jpg', 'https://img.example.com/v6c.jpg',
 45, 0, '8.5', 1, '6', '家庭,温情,催泪', '单亲妈妈含辛茹苦拉扯三个孩子，温情感人。',
 '陈秋月,林志刚', '吴导演', '国产', '2023', '普通话',
 7500, 25000, 2800, 6000, 0.50, 0, 10, 'local', 70, NOW()),

(1, '替嫁王妃不认命', 'https://img.example.com/v7.jpg', 'https://img.example.com/v7c.jpg',
 70, 0, '9.1', 1, '2,3', '古装,替嫁,逆袭', '丫鬟被迫替小姐出嫁，却意外赢得王爷真心。',
 '柳依依,燕王', '周导演', '国产', '2024', '普通话',
 18000, 60000, 5000, 10000, 1.50, 1, 4, 'local', 92, NOW()),

(1, '首席的秘密恋人', 'https://img.example.com/v8.jpg', 'https://img.example.com/v8c.jpg',
 55, 1, '8.7', 1, '1', '都市,恋爱,职场', '职场新人意外成为大老板的秘密恋人，甜蜜又虐心。',
 '安若雪,霍君', '孙导演', '国产', '2024', '普通话',
 6000, 22000, 1800, 4500, 1.00, 0, 6, 'local', 85, NOW());

-- ============================================================
-- 4. 剧集数据（每部剧前5集，前N集免费）
-- ============================================================
-- 视频1：总裁的独家宠爱（前5集免费，之后收费）
INSERT INTO `vs_drama_video_episodes`
  (`site_id`, `video_id`, `title`, `duration`, `url`, `hls_url`, `episode_num`, `is_free`, `price`, `views`, `weigh`, `status`, `create_time`)
SELECT 1, id,
  CONCAT('第', n, '集'),
  FLOOR(180 + RAND()*120),
  CONCAT('https://cdn.example.com/v1/ep', n, '.mp4'),
  CONCAT('https://cdn.example.com/v1/ep', n, '.m3u8'),
  n,
  IF(n <= 5, 1, 0),
  IF(n <= 5, 0.00, 1.00),
  FLOOR(RAND()*5000),
  81 - n,
  1,
  NOW()
FROM vs_drama_video, (
  SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
  UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) nums
WHERE title = '总裁的独家宠爱';

-- 视频2：凤凰涅槃（前3集免费）
INSERT INTO `vs_drama_video_episodes`
  (`site_id`, `video_id`, `title`, `duration`, `url`, `hls_url`, `episode_num`, `is_free`, `price`, `views`, `weigh`, `status`, `create_time`)
SELECT 1, id,
  CONCAT('第', n, '集'),
  FLOOR(200 + RAND()*100),
  CONCAT('https://cdn.example.com/v2/ep', n, '.mp4'),
  CONCAT('https://cdn.example.com/v2/ep', n, '.m3u8'),
  n,
  IF(n <= 3, 1, 0),
  IF(n <= 3, 0.00, 1.50),
  FLOOR(RAND()*8000),
  81 - n,
  1,
  NOW()
FROM vs_drama_video, (
  SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
  UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
) nums
WHERE title = '凤凰涅槃：皇后的逆袭';

-- 视频3：萌宝驾到（前10集免费）
INSERT INTO `vs_drama_video_episodes`
  (`site_id`, `video_id`, `title`, `duration`, `url`, `hls_url`, `episode_num`, `is_free`, `price`, `views`, `weigh`, `status`, `create_time`)
SELECT 1, id,
  CONCAT('第', n, '集'),
  FLOOR(150 + RAND()*90),
  CONCAT('https://cdn.example.com/v3/ep', n, '.mp4'),
  CONCAT('https://cdn.example.com/v3/ep', n, '.m3u8'),
  n,
  IF(n <= 10, 1, 0),
  IF(n <= 10, 0.00, 1.00),
  FLOOR(RAND()*3000),
  81 - n,
  1,
  NOW()
FROM vs_drama_video, (
  SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
  UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
  UNION SELECT 11 UNION SELECT 12
) nums
WHERE title = '萌宝驾到：爸爸你好';

-- 视频4-8：各5集
INSERT INTO `vs_drama_video_episodes`
  (`site_id`, `video_id`, `title`, `duration`, `url`, `hls_url`, `episode_num`, `is_free`, `price`, `views`, `weigh`, `status`, `create_time`)
SELECT 1, v.id,
  CONCAT('第', n, '集'),
  FLOOR(160 + RAND()*110),
  CONCAT('https://cdn.example.com/v', v.id, '/ep', n, '.mp4'),
  CONCAT('https://cdn.example.com/v', v.id, '/ep', n, '.m3u8'),
  n,
  IF(n <= v.free_episodes, 1, 0),
  v.price,
  FLOOR(RAND()*4000),
  81 - n,
  1,
  NOW()
FROM vs_drama_video v, (
  SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
  UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
) nums
WHERE v.title IN ('废柴学霸的翻身仗','暗夜猎手','妈妈的心愿','替嫁王妃不认命','首席的秘密恋人');

-- ============================================================
-- 5. 首页区块（Banner 轮播 + 广告位）
-- ============================================================
INSERT INTO `vs_drama_block` (`site_id`, `type`, `name`, `title`, `image`, `url`, `parsetpl`, `weigh`, `status`, `create_time`) VALUES
(1, 'focus', '主轮播1', '总裁的独家宠爱 | 火热播出', 'https://img.example.com/banner1.jpg', '/video/detail/1', 1, 100, 'normal', NOW()),
(1, 'focus', '主轮播2', '凤凰涅槃：皇后的逆袭 | 热播中',  'https://img.example.com/banner2.jpg', '/video/detail/2', 1, 90,  'normal', NOW()),
(1, 'focus', '主轮播3', '暗夜猎手 | 悬疑大作',           'https://img.example.com/banner3.jpg', '/video/detail/5', 1, 80,  'normal', NOW()),
(1, 'side',  '侧边广告1','开通VIP 畅看全集',            'https://img.example.com/ad1.jpg',     '/vip/list',      0, 100, 'normal', NOW()),
(1, 'side',  '侧边广告2','点数充值 解锁更多',            'https://img.example.com/ad2.jpg',     '/usable/list',   0, 90,  'normal', NOW());

-- ============================================================
-- 6. VIP 套餐
-- ============================================================
INSERT INTO `vs_drama_vip` (`site_id`, `title`, `description`, `content`, `days`, `price`, `original_price`, `weigh`, `status`, `create_time`) VALUES
(1, '周卡VIP',  '连续包周，畅享全站',  '全站短剧免广告、全集免费观看',  7,   6.90,  9.90,  100, 'normal', NOW()),
(1, '月卡VIP',  '连续包月，超值推荐',  '全站短剧免广告、全集免费观看', 30,  18.00, 28.00, 90,  'normal', NOW()),
(1, '季卡VIP',  '连续包季，低至每天2毛','全站短剧免广告、全集免费观看', 90,  39.00, 68.00, 80,  'normal', NOW()),
(1, '年卡VIP',  '连续包年，最超值',    '全站短剧免广告、全集免费观看',365, 108.00,198.00, 70,  'normal', NOW()),
(1, '体验卡',   '新用户首充特惠',      '全站短剧免广告、全集免费观看',  3,   0.90,  3.90,  110, 'normal', NOW());

-- ============================================================
-- 7. 点数套餐
-- ============================================================
INSERT INTO `vs_drama_usable` (`site_id`, `title`, `flag`, `description`, `usable`, `original_usable`, `give_usable`, `price`, `give_price`, `first_price`, `original_price`, `status`, `weigh`, `create_time`) VALUES
(1, '10点数',  'small',   '适合轻度用户',  10,  10,  0,  1.00,  0.00, 0.50,  2.00,  '1', 100, NOW()),
(1, '50点数',  'medium',  '最受欢迎',      55,  50,  5,  4.50,  0.00, 2.50,  8.00,  '1', 90,  NOW()),
(1, '100点数', 'large',   '性价比之选',   110, 100, 10,  8.00,  0.00, 4.00, 15.00,  '1', 80,  NOW()),
(1, '300点数', 'xlarge',  '重度用户首选', 350, 300, 50, 20.00,  0.00, 10.00, 38.00, '1', 70,  NOW()),
(1, '500点数', 'xxlarge', '包年用户专享', 600, 500,100, 30.00,  5.00, 15.00, 58.00, '1', 60,  NOW());

-- ============================================================
-- 8. 分销套餐
-- ============================================================
INSERT INTO `vs_drama_reseller` (`site_id`, `name`, `content`, `price`, `original_price`, `level`, `direct`, `indirect`, `expire`, `weigh`, `status`, `create_time`) VALUES
(1, '普通分销商', '直推佣金15%，间推5%，有效期半年', 99.00,  199.00, 1, 15.00, 5.00,  180, 100, 'normal', NOW()),
(1, '高级分销商', '直推佣金20%，间推8%，有效期一年', 299.00, 499.00, 2, 20.00, 8.00,  365, 90,  'normal', NOW()),
(1, '钻石分销商', '直推佣金25%，间推10%，有效期两年',599.00, 999.00, 3, 25.00, 10.00, 730, 80,  'normal', NOW());

-- ============================================================
-- 9. VIP 订单（已支付）
-- ============================================================
INSERT INTO `vs_drama_vip_order`
  (`site_id`, `vip_id`, `order_sn`, `user_id`, `days`, `status`, `total_fee`, `pay_fee`,
   `transaction_id`, `pay_type`, `platform`, `pay_time`, `create_time`)
SELECT
  1,
  v.id,
  CONCAT('VIP', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id*10 + v.id, 6, '0')),
  u.id,
  v.days,
  1,
  v.price,
  v.price,
  CONCAT('wx_', MD5(CONCAT(u.id, v.id, NOW()))),
  'wechat',
  'wxMiniProgram',
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*30) DAY),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*30) DAY)
FROM vs_drama_user u
JOIN vs_drama_vip v ON v.title IN ('月卡VIP','周卡VIP')
WHERE u.username IN ('user_zhang','user_wang','user_chen','user_wu','user_xu')
LIMIT 10;

-- ============================================================
-- 10. 点数套餐订单（已支付）
-- ============================================================
INSERT INTO `vs_drama_usable_order`
  (`site_id`, `usable_id`, `order_sn`, `user_id`, `usable`, `status`, `total_fee`, `pay_fee`,
   `transaction_id`, `pay_type`, `platform`, `pay_time`, `create_time`)
SELECT
  1,
  us.id,
  CONCAT('USE', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id*10 + us.id, 6, '0')),
  u.id,
  us.usable,
  1,
  us.price,
  us.price,
  CONCAT('wx_use_', MD5(CONCAT(u.id, us.id, NOW()))),
  'wechat',
  'H5',
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*15) DAY),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*15) DAY)
FROM vs_drama_user u
JOIN vs_drama_usable us ON us.title IN ('50点数','100点数')
WHERE u.username IN ('user_zhang','user_li','user_zhao','user_liu','user_sun')
LIMIT 8;

-- ============================================================
-- 11. 分销套餐订单（已支付）
-- ============================================================
INSERT INTO `vs_drama_reseller_order`
  (`site_id`, `reseller_id`, `order_sn`, `user_id`, `times`, `status`, `total_fee`, `pay_fee`,
   `transaction_id`, `pay_type`, `platform`, `pay_time`, `create_time`)
SELECT
  1,
  r.id,
  CONCAT('RES', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id*10 + r.id, 6, '0')),
  u.id,
  1,
  1,
  r.price,
  r.price,
  CONCAT('wx_res_', MD5(CONCAT(u.id, r.id, NOW()))),
  'wechat',
  'wxMiniProgram',
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*60) DAY),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*60) DAY)
FROM vs_drama_user u
JOIN vs_drama_reseller r ON r.name IN ('普通分销商','高级分销商')
WHERE u.username IN ('user_wang','user_chen','user_wu')
LIMIT 5;

-- ============================================================
-- 12. 分销绑定关系
-- ============================================================
INSERT INTO `vs_drama_reseller_bind` (`site_id`, `user_id`, `reseller_id`, `level`, `expire_time`, `create_time`)
SELECT
  1,
  u.id,
  r.id,
  r.level,
  DATE_ADD(NOW(), INTERVAL r.expire DAY),
  NOW()
FROM vs_drama_user u
JOIN vs_drama_reseller r ON (
  (u.username = 'user_wang' AND r.name = '普通分销商') OR
  (u.username = 'user_chen' AND r.name = '高级分销商') OR
  (u.username = 'user_wu'   AND r.name = '普通分销商')
);

-- ============================================================
-- 13. 钱包流水（充值、消费、佣金）
-- ============================================================
INSERT INTO `vs_drama_user_wallet_log`
  (`site_id`, `user_id`, `wallet`, `wallet_type`, `type`, `before`, `after`, `item_id`, `memo`, `create_time`)
SELECT 1, u.id, 100.00, 'usable', 'usable_recharge', 0.00, 100.00,
  CONCAT('USE', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id, 6, '0')),
  '充值100点数', DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM vs_drama_user u WHERE u.username IN ('user_zhang','user_wang','user_chen');

INSERT INTO `vs_drama_user_wallet_log`
  (`site_id`, `user_id`, `wallet`, `wallet_type`, `type`, `before`, `after`, `item_id`, `memo`, `create_time`)
SELECT 1, u.id, -1.50, 'usable', 'episode_unlock', u.usable + 1.50, u.usable,
  CONCAT('EP', u.id, '_001'),
  '解锁第3集', DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM vs_drama_user u WHERE u.username IN ('user_li','user_zhao','user_liu');

INSERT INTO `vs_drama_user_wallet_log`
  (`site_id`, `user_id`, `wallet`, `wallet_type`, `type`, `before`, `after`, `item_id`, `memo`, `create_time`)
SELECT 1, u.id, 50.00, 'money', 'reseller_commission', 0.00, 50.00,
  CONCAT('RES', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id, 6, '0')),
  '直推佣金到账', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM vs_drama_user u WHERE u.username IN ('user_wang','user_chen');

INSERT INTO `vs_drama_user_wallet_log`
  (`site_id`, `user_id`, `wallet`, `wallet_type`, `type`, `before`, `after`, `memo`, `create_time`)
SELECT 1, u.id, 200.00, 'score', 'sign_in', 0.00, 200.00, '每日签到积分', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM vs_drama_user u WHERE u.username IN ('user_zhang','user_li','user_wang','user_zhao','user_chen');

-- ============================================================
-- 14. 提现申请
-- ============================================================
INSERT INTO `vs_drama_user_wallet_apply`
  (`site_id`, `user_id`, `apply_sn`, `apply_type`, `money`, `actual_money`, `charge_money`, `service_fee`,
   `apply_info`, `status`, `create_time`)
SELECT
  1,
  u.id,
  CONCAT('DRAW', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id, 6, '0')),
  'wechat',
  100.00,
  97.00,
  3.00,
  0.030,
  JSON_OBJECT('realname','真实姓名','account', u.mobile),
  0,
  DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM vs_drama_user u WHERE u.username IN ('user_wang','user_chen');

INSERT INTO `vs_drama_user_wallet_apply`
  (`site_id`, `user_id`, `apply_sn`, `apply_type`, `money`, `actual_money`, `charge_money`, `service_fee`,
   `apply_info`, `status`, `create_time`)
SELECT
  1,
  u.id,
  CONCAT('DRAW', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(u.id*100, 6, '0')),
  'alipay',
  200.00,
  194.00,
  6.00,
  0.030,
  JSON_OBJECT('realname','真实姓名','account', u.mobile),
  2,
  DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM vs_drama_user u WHERE u.username = 'user_chen';

-- 查看插入结果
SELECT '=== 测试数据插入完成 ===' AS '';
SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'duanju' AND TABLE_NAME LIKE 'vs_%'
ORDER BY TABLE_NAME;