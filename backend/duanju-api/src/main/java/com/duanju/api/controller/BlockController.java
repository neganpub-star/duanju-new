package com.duanju.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.Block;
import com.duanju.drama.mapper.BlockMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "首页区块接口")
@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockMapper blockMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "区块列表（轮播图/广告图）")
    @GetMapping("/list")
    public R<List<Block>> list(@RequestParam(required = false) String type) {
        return R.ok(blockMapper.selectList(new LambdaQueryWrapper<Block>()
                .eq(Block::getSiteId, siteId)
                .eq(Block::getStatus, "normal")
                .eq(type != null, Block::getType, type)
                .orderByDesc(Block::getWeigh)));
    }
}
