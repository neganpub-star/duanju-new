-- 为 videoId=1（总裁夫人惹不起）添加不同格式的测试分集
-- 执行时间：2026-05-16

INSERT INTO vs_drama_video_episodes
  (site_id, video_id, episode_num, title, url, hls_url, play_info, is_free, price, status, weigh, create_time, update_time)
VALUES
-- MP4 格式（Big Buck Bunny 短片，公开测试视频）
(1, 1, 50, '测试-MP4格式',
 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
 '', NULL, 1, 0.00, 1, 50, NOW(), NOW()),

-- HLS/M3U8 格式（Apple 官方 HLS 测试流）
(1, 1, 51, '测试-HLS格式',
 '',
 'https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8',
 NULL, 1, 0.00, 1, 51, NOW(), NOW()),

-- 多清晰度 play_info（模拟转码完成后的数据）
(1, 1, 52, '测试-多清晰度',
 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
 '',
 '[{"definition":"360p","url":"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"},{"definition":"720p","url":"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"}]',
 1, 0.00, 1, 52, NOW(), NOW());
