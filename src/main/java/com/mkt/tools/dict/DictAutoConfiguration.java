package com.mkt.tools.dict;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
public class DictAutoConfiguration {

    @Bean
    public MyDictHelper getDictHelper(IMyDict myDict){
        return new MyDictHelper(myDict);
    }

}
