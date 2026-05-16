package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.api.config.I18nUtil;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.commerce.domain.UserWalletLog;
import com.duanju.commerce.mapper.UserWalletLogMapper;
import com.duanju.drama.domain.EpisodeUnlock;
import com.duanju.drama.domain.Video;
import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.domain.VideoFavorite;
import com.duanju.drama.domain.VideoLike;
import com.duanju.drama.domain.WatchLog;
import com.duanju.drama.service.VideoService;
import com.duanju.drama.mapper.CommentMapper;
import com.duanju.drama.mapper.EpisodeUnlockMapper;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import com.duanju.drama.mapper.VideoFavoriteMapper;
import com.duanju.drama.mapper.VideoLikeMapper;
import com.duanju.drama.mapper.VideoMapper;
import com.duanju.drama.mapper.WatchLogMapper;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "短剧接口")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String pickI18n(Object jsonObj, Object fallback) {
        String json = jsonObj != null ? jsonObj.toString() : null;
        String fb = fallback != null ? fallback.toString() : "";
        if (json == null || json.isBlank()) return fb;
        try {
            String lang = LocaleContextHolder.getLocale().toLanguageTag();
            @SuppressWarnings("unchecked")
            Map<String, String> map = MAPPER.readValue(json, Map.class);
            if (map.containsKey(lang)) return map.get(lang);
            String prefix = lang.split("-")[0];
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (e.getKey().startsWith(prefix)) return e.getValue();
            }
            return map.getOrDefault("zh-CN", map.values().stream().findFirst().orElse(fb));
        } catch (Exception e) {
            log.warn("pickI18n parse failed: {}", jsonObj);
            return fb;
        }
    }

    private final VideoService videoService;
    private final VideoMapper videoMapper;
    private final VideoEpisodesMapper episodesMapper;
    private final VideoFavoriteMapper favoriteMapper;
    private final VideoLikeMapper videoLikeMapper;
    private final CommentMapper commentMapper;
    private final WatchLogMapper watchLogMapper;
    private final DramaUserMapper userMapper;
    private final EpisodeUnlockMapper unlockMapper;
    private final UserWalletLogMapper walletLogMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "短剧列表")
    @GetMapping("/list")
    public R<List<Video>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(name = "page", required = false) Integer page,          // 旧前端用 page
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pagesize", required = false) Integer pagesize,  // 旧前端用 pagesize
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(name = "category_id", required = false) Long categoryIdOld) { // 旧前端用 category_id
        int pNum = (page != null) ? page : pageNum;
        int pSize = (pagesize != null) ? pagesize : pageSize;
        Long catId = (categoryIdOld != null) ? categoryIdOld : categoryId;
        PageQuery pq = new PageQuery();
        pq.setPageNum(pNum);
        pq.setPageSize(pSize);
        PageResult<Video> result = videoService.pageList(pq, siteId, keyword, catId, 1);
        List<Video> rows = result.getRows();
        // 默认 is_favorite=0，登录用户叠加实际收藏状态
        rows.forEach(v -> v.setIsFavorite(0));
        if (StpUtil.isLogin() && !rows.isEmpty()) {
            long userId = StpUtil.getLoginIdAsLong();
            List<Long> favIds = favoriteMapper.selectList(new LambdaQueryWrapper<VideoFavorite>()
                    .eq(VideoFavorite::getUserId, userId)
                    .select(VideoFavorite::getVideoId))
                    .stream().map(VideoFavorite::getVideoId).collect(Collectors.toList());
            if (!favIds.isEmpty()) {
                rows.forEach(v -> v.setIsFavorite(favIds.contains(v.getId()) ? 1 : 0));
            }
        }
        return R.ok(rows);
    }

    @Operation(summary = "短剧详情（含分集列表）")
    @GetMapping({"/detail/{pathId}", "/detail"})
    public R<VideoDetailResp> detail(@PathVariable(required = false) Long pathId,
                                     @RequestParam(required = false) Long id,
                                     @RequestParam(required = false) Long id2) {
        Long videoId = pathId != null ? pathId : (id != null ? id : id2);
        if (videoId == null) return R.fail(I18nUtil.msg("error.video.id.missing"));
        Video video = videoService.getDetail(videoId);
        if (video == null) return R.fail(I18nUtil.msg("error.video.not.found"));
        List<VideoEpisodes> episodes = episodesMapper.selectByVideoId(videoId);

        // 查询当前用户是否已收藏、已点赞
        boolean isFav = false;
        boolean isLiked = false;
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            isFav = favoriteMapper.selectCount(new LambdaQueryWrapper<VideoFavorite>()
                    .eq(VideoFavorite::getUserId, userId)
                    .eq(VideoFavorite::getVideoId, videoId)) > 0;
            isLiked = videoLikeMapper.existsByUserAndVideo(userId, videoId) > 0;
        }

        VideoDetailResp resp = new VideoDetailResp();
        resp.setId(video.getId());
        resp.setTitle(video.getTitle());
        resp.setDisplayTitle(video.getDisplayTitle());
        String coverUrl = video.getCover() != null ? video.getCover() : video.getImage();
        resp.setImage(coverUrl);
        resp.setCover(coverUrl);
        resp.setDescription(video.getDescription());
        resp.setDisplayDesc(video.getDisplayDesc());
        resp.setEpisodes(video.getSeriesCount());
        resp.setEpisodeCount(video.getSeriesCount());
        resp.setFavorites(favoriteMapper.countByVideoId(videoId));
        resp.setIsFavorite(isFav);
        resp.setIsLike(isLiked);
        resp.setLikes(video.getLikes() != null ? video.getLikes() : 0);
        resp.setShares(video.getShares() != null ? video.getShares() : 0);
        resp.setComments(commentMapper.countTopComments(videoId));
        List<EpisodeVO> episodeVOs = episodes.stream().map(EpisodeVO::from).collect(Collectors.toList());

        // 判断当前用户的访问权限，隐藏无权限分集的URL（强制走 /play 鉴权）
        boolean userIsVip = false;
        java.util.Set<Long> unlockedEpIds = new java.util.HashSet<>();
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            DramaUser u = userMapper.selectById(userId);
            userIsVip = u != null && u.isVipActive();
            // 查已解锁的分集
            unlockMapper.selectList(
                    new LambdaQueryWrapper<EpisodeUnlock>()
                            .eq(EpisodeUnlock::getUserId, userId)
                            .eq(EpisodeUnlock::getVideoId, videoId)
                            .isNull(EpisodeUnlock::getDeleteTime))
                    .forEach(ul -> unlockedEpIds.add(ul.getEpisodeId()));
        }
        // VIP用户且该剧是VIP剧 → 全部放行
        boolean vipFullAccess = userIsVip && video.getIsVip() != null && video.getIsVip() == 1;
        for (EpisodeVO epVO : episodeVOs) {
            if (epVO.getIsFree() != null && epVO.getIsFree() == 1) continue;  // 免费集
            if (vipFullAccess) continue;                                        // VIP全访问
            if (unlockedEpIds.contains(epVO.getId())) continue;                // 已单集解锁
            // 否则隐藏URL，前端必须走 /play 接口
            epVO.setUrl(null);
            epVO.setHlsUrl(null);
            epVO.setPlayInfo(null);
        }

        resp.setEpisodesList(episodeVOs);
        return R.ok(resp);
    }

    @Operation(summary = "剧集列表")
    @GetMapping({"/episodes/{videoId}", "/episodes"})
    public R<List<EpisodeVO>> episodes(@PathVariable(required = false) Long videoId,
                                       @RequestParam(required = false) Long vid) {
        Long id = videoId != null ? videoId : vid;
        if (id == null) return R.fail(I18nUtil.msg("error.video.id.missing"));
        List<VideoEpisodes> episodeList = episodesMapper.selectByVideoId(id);
        List<EpisodeVO> vos = episodeList.stream().map(EpisodeVO::from).collect(Collectors.toList());

        // 与 /detail 保持一致：对无权限的分集隐藏播放 URL
        Video video = videoService.getDetail(id);
        boolean userIsVip = false;
        java.util.Set<Long> unlockedEpIds = new java.util.HashSet<>();
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            DramaUser u = userMapper.selectById(userId);
            userIsVip = u != null && u.isVipActive();
            unlockMapper.selectList(
                    new LambdaQueryWrapper<EpisodeUnlock>()
                            .eq(EpisodeUnlock::getUserId, userId)
                            .eq(EpisodeUnlock::getVideoId, id)
                            .isNull(EpisodeUnlock::getDeleteTime))
                    .forEach(ul -> unlockedEpIds.add(ul.getEpisodeId()));
        }
        boolean vipFullAccess = userIsVip && video != null && video.getIsVip() != null && video.getIsVip() == 1;
        for (EpisodeVO epVO : vos) {
            if (epVO.getIsFree() != null && epVO.getIsFree() == 1) continue;
            if (vipFullAccess) continue;
            if (unlockedEpIds.contains(epVO.getId())) continue;
            epVO.setUrl(null);
            epVO.setHlsUrl(null);
            epVO.setPlayInfo(null);
        }
        return R.ok(vos);
    }

    @Operation(summary = "推荐短剧（含第一集 URL，兼容旧前端滑屏播放）")
    @GetMapping("/recommend")
    public R<List<Map<String, Object>>> recommend(@RequestParam(defaultValue = "10") Integer limit) {
        List<Video> videos = videoService.recommend(siteId, limit);
        // 查当前用户收藏状态与VIP状态
        java.util.Set<Long> favIds = new java.util.HashSet<>();
        boolean userIsVip = false;
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            favoriteMapper.selectList(new LambdaQueryWrapper<VideoFavorite>()
                    .eq(VideoFavorite::getUserId, userId)
                    .select(VideoFavorite::getVideoId))
                    .forEach(f -> favIds.add(f.getVideoId()));
            DramaUser u = userMapper.selectById(userId);
            userIsVip = u != null && u.isVipActive();
        }
        final boolean isVip = userIsVip;
        List<Map<String, Object>> result = videos.stream().map(v -> {
            String cover = v.getCover() != null ? v.getCover() : v.getImage();
            List<VideoEpisodes> eps = episodesMapper.selectByVideoId(v.getId());
            VideoEpisodes ep1 = (eps != null && !eps.isEmpty()) ? eps.get(0) : null;

            // 判断 ep1 是否对当前用户可播放
            String ep1Url = null;
            String ep1HlsUrl = null;
            if (ep1 != null) {
                boolean ep1Free = ep1.getIsFree() != null && ep1.getIsFree() == 1;
                boolean vipAccess = isVip && v.getIsVip() != null && v.getIsVip() == 1;
                if (ep1Free || vipAccess) {
                    ep1Url = ep1.getUrl() != null ? ep1.getUrl() : ep1.getHlsUrl();
                    ep1HlsUrl = ep1.getHlsUrl();
                }
            }

            // video 子对象（模板通过 item.video.xxx 访问）
            Map<String, Object> video = new LinkedHashMap<>();
            video.put("id", v.getId());
            video.put("title", v.getTitle());
            video.put("image", cover);
            video.put("cover", cover);
            video.put("description", v.getDescription());
            video.put("episodes", v.getSeriesCount());
            video.put("is_favorite", favIds.contains(v.getId()) ? 1 : 0);
            video.put("favorites", favoriteMapper.countByVideoId(v.getId()));
            video.put("shares", v.getShares() != null ? v.getShares() : 0);

            // 外层条目（模板通过 item.id / item.vid / item.url / item.name 等访问）
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",    ep1 != null ? ep1.getId() : v.getId());
            m.put("vid",   v.getId());
            m.put("url",   ep1Url);
            m.put("hlsUrl", ep1HlsUrl);
            m.put("image", cover);
            m.put("name",  ep1 != null ? (ep1.getTitle() != null ? ep1.getTitle() : "第1集") : "第1集");
            m.put("is_like", 0);
            m.put("likes", v.getLikes() != null ? v.getLikes() : 0);
            m.put("shares", v.getShares() != null ? v.getShares() : 0);
            m.put("adsTrue", false);
            m.put("video", video);
            return m;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    @Operation(summary = "观看/追剧历史列表")
    @GetMapping("/history")
    public R<List<Map<String, Object>>> history(
            @RequestParam(defaultValue = "log") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        if (!StpUtil.isLogin()) return R.ok(Collections.emptyList());
        long userId = StpUtil.getLoginIdAsLong();
        int offset = (page - 1) * pagesize;

        List<Map<String, Object>> result = new ArrayList<>();

        if ("favorite".equals(type)) {
            List<Map<String, Object>> items = favoriteMapper.selectFavoriteList(userId, offset, pagesize);
            for (Map<String, Object> item : items) {
                Object coverObj = item.get("cover") != null ? item.get("cover") : item.get("image");
                String cover = coverObj != null ? coverObj.toString() : "";
                Map<String, Object> video = new LinkedHashMap<>();
                video.put("id", item.get("videoId"));
                video.put("title", item.get("title"));
                video.put("display_title", pickI18n(item.get("titleI18n"), item.get("title")));
                video.put("image", cover);
                video.put("cover", cover);
                video.put("description", item.get("description"));
                video.put("display_desc", pickI18n(item.get("descI18n"), item.get("description")));
                video.put("episodes", item.get("seriesCount"));
                Map<String, Object> episode = new LinkedHashMap<>();
                episode.put("name", "第1集");
                episode.put("display_title", pickI18n("{\"zh-CN\":\"第1集\",\"zh-TW\":\"第1集\",\"en\":\"Episode 1\"}", "第1集"));
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("vid", item.get("videoId"));
                row.put("video", video);
                row.put("episode", episode);
                result.add(row);
            }
        } else {
            // type=log: 观看记录
            List<Map<String, Object>> items = watchLogMapper.selectWatchLogList(userId, offset, pagesize);
            for (Map<String, Object> item : items) {
                Object coverObj = item.get("cover") != null ? item.get("cover") : item.get("image");
                String cover = coverObj != null ? coverObj.toString() : "";
                Map<String, Object> video = new LinkedHashMap<>();
                video.put("id", item.get("videoId"));
                video.put("title", item.get("title"));
                video.put("display_title", pickI18n(item.get("titleI18n"), item.get("title")));
                video.put("image", cover);
                video.put("cover", cover);
                video.put("description", item.get("description"));
                video.put("display_desc", pickI18n(item.get("descI18n"), item.get("description")));
                video.put("episodes", item.get("seriesCount"));
                Map<String, Object> episode = new LinkedHashMap<>();
                String epName = item.get("episodeName") != null ? item.get("episodeName").toString() : "第1集";
                episode.put("name", epName);
                episode.put("display_title", pickI18n(item.get("episodeTitleI18n"), epName));
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("vid", item.get("videoId"));
                row.put("video", video);
                row.put("episode", episode);
                result.add(row);
            }
        }
        return R.ok(result);
    }

    @Operation(summary = "保存观看进度（type=log）/ 追剧（type=favorite）")
    @PostMapping("/record")
    public R<Void> saveRecord(@RequestBody RecordReq req) {
        if (!StpUtil.isLogin()) return R.ok();
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = req.getVid() != null ? toLong(req.getVid()) : null;
        if (videoId == null) return R.ok();

        if ("favorite".equals(req.getType())) {
            // 追剧逻辑
            VideoFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<VideoFavorite>()
                    .eq(VideoFavorite::getUserId, userId).eq(VideoFavorite::getVideoId, videoId));
            if (existing == null) {
                VideoFavorite fav = new VideoFavorite();
                fav.setSiteId(siteId);
                fav.setUserId(userId);
                fav.setVideoId(videoId);
                favoriteMapper.insert(fav);
            }
        } else {
            // 观看进度记录
            Long episodeId = req.getEpisodeId() != null ? toLong(req.getEpisodeId()) : 0L;
            BigDecimal viewTime = req.getViewTime() != null ? new BigDecimal(req.getViewTime().toString()) : BigDecimal.ZERO;
            WatchLog existing = watchLogMapper.selectOne(new LambdaQueryWrapper<WatchLog>()
                    .eq(WatchLog::getUserId, userId).eq(WatchLog::getVideoId, videoId));
            if (existing == null) {
                WatchLog log = new WatchLog();
                log.setSiteId(siteId);
                log.setUserId(userId);
                log.setVideoId(videoId);
                log.setEpisodeId(episodeId);
                log.setViewTime(viewTime);
                watchLogMapper.insert(log);
            } else {
                existing.setEpisodeId(episodeId);
                existing.setViewTime(viewTime);
                watchLogMapper.updateById(existing);
            }
        }
        return R.ok();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
    }

    @Operation(summary = "添加追剧收藏")
    @PostMapping("/favorite")
    public R<Map<String, Object>> addFavorite(@RequestBody FavoriteReq req) {
        if (!StpUtil.isLogin()) return R.fail(I18nUtil.msg("error.login.required"));
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = req.getVidLong() != null ? req.getVidLong() : req.getVideoIdLong();
        if (videoId == null) return R.fail(I18nUtil.msg("error.video.id.missing"));

        VideoFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<VideoFavorite>()
                .eq(VideoFavorite::getUserId, userId)
                .eq(VideoFavorite::getVideoId, videoId));
        if (existing == null) {
            try {
                VideoFavorite fav = new VideoFavorite();
                fav.setSiteId(siteId);
                fav.setUserId(userId);
                fav.setVideoId(videoId);
                favoriteMapper.insert(fav);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 软删除记录存在，恢复它
                favoriteMapper.restoreByUserAndVideo(userId, videoId);
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("is_favorite", 1);
        return R.ok(resp);
    }

    @Operation(summary = "取消追剧收藏")
    @PostMapping("/favorite/remove")
    public R<Void> removeFavorite(@RequestBody FavoriteReq req) {
        if (!StpUtil.isLogin()) return R.ok();
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = req.getIdsLong() != null ? req.getIdsLong() : req.getVidLong();
        if (videoId == null) return R.ok();
        favoriteMapper.delete(new LambdaQueryWrapper<VideoFavorite>()
                .eq(VideoFavorite::getUserId, userId)
                .eq(VideoFavorite::getVideoId, videoId));
        return R.ok();
    }

    @Data
    public static class FavoriteReq {
        private Object vid;
        @JsonProperty("video_id")
        private Object videoId;
        private Object ids;
        private String type;

        public Long getVidLong() { return toLongStatic(vid); }
        public Long getVideoIdLong() { return toLongStatic(videoId); }
        public Long getIdsLong() { return toLongStatic(ids); }

        private static Long toLongStatic(Object val) {
            if (val == null) return null;
            try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
        }
    }

    @Data
    public static class RecordReq {
        private Object vid;
        @JsonProperty("episode_id")
        private Object episodeId;
        private String type;
        @JsonProperty("view_time")
        private Object viewTime;
        private String platform;
        private Integer addlog;
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/likes")
    public R<Map<String, Object>> toggleLike(@RequestBody Map<String, Object> req) {
        Long videoId = toLong(req.get("vid"));
        String action = req.get("action") != null ? req.get("action").toString() : "like";
        if (videoId == null) return R.fail(I18nUtil.msg("error.video.id.missing"));

        // 未登录：直接返回成功但不更改任何数据
        if (!StpUtil.isLogin()) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("is_like", "like".equals(action) ? 1 : 0);
            return R.ok(resp);
        }

        long userId = StpUtil.getLoginIdAsLong();
        boolean alreadyLiked = videoLikeMapper.existsByUserAndVideo(userId, videoId) > 0;
        boolean isNowLiked;

        if ("like".equals(action)) {
            if (!alreadyLiked) {
                VideoLike like = new VideoLike();
                like.setSiteId(siteId);
                like.setUserId(userId);
                like.setVideoId(videoId);
                like.setCreateTime(java.time.LocalDateTime.now());
                videoLikeMapper.insert(like);
                videoMapper.updateLikesCount(videoId, 1);
            }
            isNowLiked = true;
        } else {
            if (alreadyLiked) {
                videoLikeMapper.deleteByUserAndVideo(userId, videoId);
                videoMapper.updateLikesCount(videoId, -1);
            }
            isNowLiked = false;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("is_like", isNowLiked ? 1 : 0);
        return R.ok(resp);
    }

    @Operation(summary = "记录分享")
    @PostMapping("/share")
    public R<Void> recordShare(@RequestBody Map<String, Object> req) {
        Long videoId = toLong(req.get("vid"));
        if (videoId != null) {
            videoMapper.incrementSharesCount(videoId);
        }
        return R.ok();
    }

    @Operation(summary = "记录观看")
    @PostMapping({"/view/{id}", "/view"})
    public R<Void> addView(@PathVariable(required = false) Long id,
                           @RequestParam(required = false) Long vid) {
        Long videoId = id != null ? id : vid;
        if (videoId != null) videoService.addViewCount(videoId);
        return R.ok();
    }

    @Operation(summary = "获取分集播放URL（兼容旧前端）")
    @PostMapping("/play")
    public R<EpisodePlayResp> play(@RequestBody PlayReq req) {
        if (req.getEpisodeId() == null) return R.fail(I18nUtil.msg("error.episode.id.missing"));
        VideoEpisodes ep = episodesMapper.selectById(req.getEpisodeId());
        if (ep == null) return R.fail(I18nUtil.msg("error.episode.not.found"));

        // is_free=1 直接放行
        if (ep.getIsFree() != null && ep.getIsFree() == 1) {
            EpisodePlayResp resp = new EpisodePlayResp();
            resp.setUrl(ep.getUrl() != null ? ep.getUrl() : ep.getHlsUrl());
            resp.setHlsUrl(ep.getHlsUrl());
            return R.ok(resp);
        }

        // 付费集：必须登录
        if (!StpUtil.isLogin()) {
            return R.fail(403, I18nUtil.msg("error.episode.login.required"));
        }
        long userId = StpUtil.getLoginIdAsLong();

        // 已单集解锁 → 放行
        if (unlockMapper.existsUnlock(userId, ep.getId()) > 0) {
            EpisodePlayResp resp = new EpisodePlayResp();
            resp.setUrl(ep.getUrl() != null ? ep.getUrl() : ep.getHlsUrl());
            resp.setHlsUrl(ep.getHlsUrl());
            return R.ok(resp);
        }

        // VIP用户且该剧是VIP剧 → 放行
        Video video = videoService.getDetail(ep.getVideoId());
        if (video != null && video.getIsVip() != null && video.getIsVip() == 1) {
            DramaUser user = userMapper.selectById(userId);
            if (user != null && user.isVipActive()) {
                EpisodePlayResp resp = new EpisodePlayResp();
                resp.setUrl(ep.getUrl() != null ? ep.getUrl() : ep.getHlsUrl());
                resp.setHlsUrl(ep.getHlsUrl());
                return R.ok(resp);
            }
        }

        // 尝试积分自动解锁
        BigDecimal price = ep.getPrice() != null ? ep.getPrice() : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            // price=0 且 is_free=0 → VIP专属集，不可单集购买，前面VIP判断已放行过，到这里说明非VIP
            return R.fail(403, I18nUtil.msg("error.episode.vip.required"));
        }

        DramaUser user = userMapper.selectById(userId);
        if (user == null || user.getUsable().compareTo(price) < 0) {
            return R.fail(403, I18nUtil.msg("error.insufficient.usable", price.stripTrailingZeros().toPlainString()));
        }
        // 原子扣积分（余额不足时 UPDATE 影响行数为0）
        int affected = userMapper.deductUsable(userId, price);
        if (affected == 0) {
            return R.fail(403, I18nUtil.msg("error.insufficient.usable", price.stripTrailingZeros().toPlainString()));
        }
        // 记录解锁
        EpisodeUnlock unlock = new EpisodeUnlock();
        unlock.setSiteId(siteId);
        unlock.setUserId(userId);
        unlock.setVideoId(ep.getVideoId());
        unlock.setEpisodeId(ep.getId());
        unlock.setPrice(price);
        unlockMapper.insert(unlock);
        // 写钱包日志
        UserWalletLog log = new UserWalletLog();
        log.setSiteId(siteId);
        log.setUserId(userId);
        log.setWallet(price.negate());
        log.setWalletType("usable");
        log.setType("episode_unlock");
        log.setBefore(user.getUsable());
        log.setAfter(user.getUsable().subtract(price));
        log.setItemId(String.valueOf(ep.getId()));
        log.setMemo(I18nUtil.msg("wallet.episode.unlock", ep.getEpisodeNum()));
        walletLogMapper.insert(log);

        EpisodePlayResp resp = new EpisodePlayResp();
        resp.setUrl(ep.getUrl() != null ? ep.getUrl() : ep.getHlsUrl());
        resp.setHlsUrl(ep.getHlsUrl());
        return R.ok(resp);
    }

    /** 分集 VO — 兼容旧前端字段名 */
    @Data
    public static class EpisodeVO {
        private Long id;
        private Long videoId;
        private String name;            // 旧前端用 name
        private String title;
        @JsonProperty("display_title")
        private String displayTitle;
        private String url;
        private String hlsUrl;
        @JsonRawValue
        private String playInfo;        // 多清晰度播放信息
        private Integer episodeNum;
        @JsonProperty("episode_num")
        private Integer episode_num;    // 旧前端用 episode_num
        private Integer isFree;
        @JsonProperty("is_free")
        private Integer is_free;        // 旧前端用 is_free
        private Integer duration;
        private Integer status;
        private BigDecimal price;

        public static EpisodeVO from(VideoEpisodes ep) {
            EpisodeVO vo = new EpisodeVO();
            vo.id = ep.getId();
            vo.videoId = ep.getVideoId();
            vo.name = ep.getTitle();
            vo.title = ep.getTitle();
            vo.displayTitle = ep.getDisplayTitle();
            vo.playInfo = ep.getPlayInfo();
            // 播放 URL 优先级：playInfo 最高清晰度 → hlsUrl → url
            vo.url = resolveBestUrl(ep.getPlayInfo(), ep.getHlsUrl(), ep.getUrl());
            vo.hlsUrl = ep.getHlsUrl();
            vo.episodeNum = ep.getEpisodeNum();
            vo.episode_num = ep.getEpisodeNum();
            vo.isFree = ep.getIsFree();
            vo.is_free = ep.getIsFree();
            vo.duration = ep.getDuration();
            vo.status = ep.getStatus();
            vo.price = ep.getPrice();
            return vo;
        }

        /** 按优先级解析最佳播放 URL */
        private static String resolveBestUrl(String playInfo, String hlsUrl, String url) {
            if (playInfo != null && !playInfo.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode arr =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(playInfo);
                    if (arr.isArray() && arr.size() > 0) {
                        String best = arr.get(arr.size() - 1).path("url").asText("");
                        if (!best.isEmpty()) return best;
                    }
                } catch (Exception ignored) {}
            }
            if (hlsUrl != null && !hlsUrl.isEmpty()) return hlsUrl;
            return url;
        }

        public void setUrl(String url)         { this.url = url; }
        public void setHlsUrl(String hlsUrl)   { this.hlsUrl = hlsUrl; }
        public void setPlayInfo(String p)      { this.playInfo = p; }
        public Integer getIsFree()             { return isFree; }
        public Long getId()                    { return id; }
    }

    @Data
    public static class VideoDetailResp {
        private Long id;
        private String title;
        @JsonProperty("display_title")
        private String displayTitle;
        private String image;
        private String cover;
        private String description;
        @JsonProperty("display_desc")
        private String displayDesc;
        private Integer episodes;
        private Integer episodeCount;
        private Integer favorites;
        @JsonProperty("is_favorite")
        private Boolean isFavorite;
        @JsonProperty("is_like")
        private Boolean isLike;
        private Integer likes;
        private Integer shares;
        private Integer comments;
        private Object viewTime;
        private Long episodeId;
        @JsonProperty("episodes_list")
        private List<EpisodeVO> episodesList;
    }

    @Data
    public static class PlayReq {
        private Long vid;
        @JsonProperty("episode_id")
        private Long episodeId;
        private String platform;
    }

    @Data
    public static class EpisodePlayResp {
        private String url;
        private String hlsUrl;
    }
}
