package com.duanju.system.service;

import com.duanju.system.domain.SysConfig;

import java.util.List;
import java.util.Map;

public interface SysConfigService {

    /**
     * 根据 key 获取配置值，不存在时返回 defaultValue
     */
    String getValue(String key, String defaultValue);

    /**
     * 获取全部配置列表（按分组+key排序）
     */
    List<SysConfig> listAll();

    /**
     * 批量更新配置值
     */
    void batchUpdate(Map<String, String> kvMap);

    /**
     * 新增配置项
     */
    void create(SysConfig config);

    /**
     * 删除配置项
     */
    void delete(String key);
}
