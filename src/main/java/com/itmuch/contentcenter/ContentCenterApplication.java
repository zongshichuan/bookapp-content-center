package com.itmuch.contentcenter;

import com.itmuch.contentcenter.configuration.UserCenterFeignConfiguration;
import com.itmuch.contentcenter.restinterceptor.TestRestTemplateTokenRelayInterceptor;
import com.itmuch.contentcenter.rocketmq.MySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.Collections;

//包扫描mapper，否则创建mapper时会找不到
@MapperScan("com.itmuch.contentcenter.dao")
@SpringBootApplication
//@EnableFeignClients(defaultConfiguration = UserCenterFeignConfiguration.class)//否则会找不到@FeignClient注释的类
@EnableFeignClients//否则会找不到@FeignClient注释的类

@EnableBinding({Source.class, MySource.class})
public class ContentCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentCenterApplication.class, args);
    }

    /**
     * @Bean指，在spring容器中创建一个类型为RestTemplate的对象
     * @return
     */
    @Bean
    @LoadBalanced  //为restTemplate整合ribbon
    @SentinelRestTemplate //为restTemplate整合sentinel
    public RestTemplate restTemplate(){
//        return new RestTemplate();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                Collections.singletonList(
                        new TestRestTemplateTokenRelayInterceptor()
                )
        );
        return restTemplate;
    }

}
