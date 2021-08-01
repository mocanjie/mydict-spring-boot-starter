package com.gcloud.tools.dict;

import com.gcloud.tools.dict.utils.MyDictUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
public class DictAutoConfiguration {

    @Bean
    public MyDictUtils getMyDictUtils(){
        return new MyDictUtils();
    }

}
