package io.github.mocanjie.tools.dict;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyDict 自动配置类
 * 自动装配字典帮助类和缓存配置
 *
 * @author mocanjie
 */
@Configuration
@EnableConfigurationProperties(MyDictCacheProperties.class)
public class DictAutoConfiguration {

    /**
     * 创建 MyDictCacheProperties Bean
     * 如果用户没有自定义配置，使用默认配置
     */
    @Bean
    @ConditionalOnMissingBean
    public MyDictCacheProperties myDictCacheProperties() {
        return new MyDictCacheProperties();
    }

    /**
     * 创建 MyDictHelper Bean
     * 自动注入 IMyDict 实现和缓存配置
     */
    @Bean
    public MyDictHelper getDictHelper(IMyDict myDict, MyDictCacheProperties cacheProperties) {
        return new MyDictHelper(myDict, cacheProperties);
    }

}
