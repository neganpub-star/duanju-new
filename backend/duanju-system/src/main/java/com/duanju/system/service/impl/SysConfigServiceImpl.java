package com.duanju.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.system.domain.SysConfig;
import com.duanju.system.mapper.SysConfigMapper;
import com.duanju.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper configMapper;

    @Override
    public String getValue(String key, String defaultValue) {
        SysConfig config = configMapper.selectById(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public List<SysConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigGroup, SysConfig::getConfigKey));
    }

    @Override
    public void batchUpdate(Map<String, String> kvMap) {
        kvMap.forEach((k, v) -> {
            SysConfig config = new SysConfig();
            config.setConfigKey(k);
            config.setConfigValue(v);
            int rows = configMapper.updateById(config);
            log.info("更新系统参数, key={}, rows={}", k, rows);
        });
    }

    @Override
    public void create(SysConfig config) {
        configMapper.insert(config);
        log.info("新增系统参数, key={}", config.getConfigKey());
    }

    @Override
    public void delete(String key) {
        configMapper.deleteById(key);
        log.info("删除系统参数, key={}", key);
    }
}
