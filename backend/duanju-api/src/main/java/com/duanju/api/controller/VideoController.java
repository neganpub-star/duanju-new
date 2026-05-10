package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.drama.domain.Video;
import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.service.VideoService;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "短剧接口")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final VideoEpisodesMapper episodesMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "短剧列表")
    @GetMapping("/list")
    public R<PageResult<Video>> list(PageQuery pageQuery,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Long categoryId) {
        return R.ok(videoService.pageList(pageQuery, siteId, keyword, categoryId, 1));
    }

    @Operation(summary = "短剧详情")
    @GetMapping("/detail/{id}")
    public R<Video> detail(@PathVariable Long id) {
        return R.ok(videoService.getDetail(id));
    }

    @Operation(summary = "剧集列表")
    @GetMapping("/episodes/{videoId}")
    public R<List<VideoEpisodes>> episodes(@PathVariable Long videoId) {
        return R.ok(episodesMapper.selectByVideoId(videoId));
    }

    @Operation(summary = "推荐短剧")
    @GetMapping("/recommend")
    public R<List<Video>> recommend(@RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(videoService.recommend(siteId, limit));
    }

    @Operation(summary = "记录观看")
    @PostMapping("/view/{id}")
    public R<Void> addView(@PathVariable Long id) {
        videoService.addViewCount(id);
        return R.ok();
    }
}
