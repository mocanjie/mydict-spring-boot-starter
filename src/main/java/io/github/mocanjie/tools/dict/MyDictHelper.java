package io.github.mocanjie.tools.dict;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.concurrent.TimeUnit;

/**
 * MyDict 字典帮助类
 * 支持 Caffeine 缓存，提升字典查询性能
 *
 * @author mocanjie
 */
public class MyDictHelper {

    private static IMyDict myDict;
    private static Cache<String, String> cache;
    private static MyDictCacheProperties cacheProperties;

    public MyDictHelper(IMyDict myDict, MyDictCacheProperties properties) {
        MyDictHelper.myDict = myDict;
        MyDictHelper.cacheProperties = properties;

        // 初始化缓存
        if (properties.isEnabled()) {
            Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                    .expireAfterWrite(properties.getTtl(), TimeUnit.SECONDS)
                    .maximumSize(properties.getMaxSize());

            // 如果启用统计，记录缓存命中率
            if (properties.isRecordStats()) {
                cacheBuilder.recordStats();
            }

            cache = cacheBuilder.build();
        }
    }

    /**
     * 获取字典描述
     * 如果启用缓存，会自动缓存查询结果
     *
     * @param name 字典名称
     * @param value 字典值
     * @return 字典描述
     */
    public static String getDesc(String name, Object value) {
        // 如果缓存未启用，直接查询
        if (cache == null) {
            return myDict.getDesc(name, String.valueOf(value));
        }

        // 构造缓存 key: name:value
        String cacheKey = name + ":" + value;

        // 从缓存获取，如果不存在则查询并缓存
        return cache.get(cacheKey, key -> myDict.getDesc(name, String.valueOf(value)));
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * 清除指定字典名称的所有缓存
     *
     * @param name 字典名称
     */
    public static void clearCache(String name) {
        if (cache != null) {
            cache.asMap().keySet().removeIf(key -> key.startsWith(name + ":"));
        }
    }

    /**
     * 获取缓存统计信息
     * 需要配置 mydict.cache.record-stats=true
     *
     * @return 缓存统计信息
     */
    public static CacheStats getCacheStats() {
        if (cache != null) {
            return cache.stats();
        }
        return null;
    }

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    public static MyDictCacheProperties getCacheProperties() {
        return cacheProperties;
    }
}
