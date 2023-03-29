package com.itmuch.contentcenter.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign的配置类
 *
 *
 * 注意：这个类不要添加@Configuration注解，如果要写，和我们定义的ribbonconfiguration.RibbonConfiguration一样，
 *       需要把这个类放到启动类所在包之外，否则会出现父子上下文扫描的问题
 */
//@Configuration
public class UserCenterFeignConfiguration {

    @Bean
    public Logger.Level level(){
        /**
         *把Feign的日志级别设置为FULL:
         * 1、FULL:记录请求和响应的header、body和元数据
         */
        return Logger.Level.FULL;
    }
}
