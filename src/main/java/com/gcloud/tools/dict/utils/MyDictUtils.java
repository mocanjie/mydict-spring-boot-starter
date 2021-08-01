package com.gcloud.tools.dict.utils;

import com.gcloud.tools.dict.handler.IMyDictHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class MyDictUtils implements ApplicationContextAware, InitializingBean {

    static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MyDictUtils.applicationContext = applicationContext;
    }

    public static String getDictDescribe(String name,Object value){
        return  applicationContext.getBean(IMyDictHandler.class).getDictDescribe(name,String.valueOf(value));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(applicationContext==null) throw new IllegalArgumentException("applicationContext初始化失败");
        if(applicationContext.getBean(IMyDictHandler.class)==null) throw new IllegalArgumentException("====没有找到IMyDictHandler的实现类====");
    }
}
