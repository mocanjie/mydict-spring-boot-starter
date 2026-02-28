package io.github.mocanjie.tools.dict;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyDict 缓存配置属性
 *
 * @author mocanjie
 */
@ConfigurationProperties(prefix = "mydict.cache")
public class MyDictCacheProperties {

    /**
     * 是否启用缓存
     * 默认：true
     */
    private boolean enabled = true;

    /**
     * 缓存过期时间（秒）
     * 默认：30秒
     */
    private long ttl = 30;

    /**
     * 缓存最大条目数
     * 默认：10000
     */
    private long maxSize = 10000;

    /**
     * 是否记录缓存统计信息
     * 默认：false
     */
    private boolean recordStats = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }
}
