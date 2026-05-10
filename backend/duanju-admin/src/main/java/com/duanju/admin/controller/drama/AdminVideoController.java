package com.duanju.admin.controller.drama;

import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.drama.domain.Video;
import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import com.duanju.drama.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "【后管】短剧管理")
@RestController
@RequestMapping("/admin/drama/video")
@RequiredArgsConstructor
public class AdminVideoController {

    private final VideoService videoService;
    private final VideoEpisodesMapper episodesMapper;

    @Operation(summary = "短剧列表")
    @GetMapping("/list")
    public R<PageResult<Video>> list(PageQuery pageQuery,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") Integer siteId) {
        return R.ok(videoService.pageList(pageQuery, siteId, keyword, categoryId, status));
    }

    @Operation(summary = "短剧详情")
    @GetMapping("/{id}")
    public R<Video> detail(@PathVariable Long id) {
        return R.ok(videoService.getDetail(id));
    }

    @Operation(summary = "新增短剧")
    @PostMapping
    public R<Void> add(@Valid @RequestBody Video video) {
        videoService.saveVideo(video);
        return R.ok();
    }

    @Operation(summary = "修改短剧")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Video video) {
        video.setId(id);
        videoService.updateVideo(video);
        return R.ok();
    }

    @Operation(summary = "删除短剧")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        videoService.removeVideo(id);
        return R.ok();
    }

    @Operation(summary = "剧集列表")
    @GetMapping("/{videoId}/episodes")
    public R<List<VideoEpisodes>> episodes(@PathVariable Long videoId) {
        return R.ok(episodesMapper.selectByVideoId(videoId));
    }
}
