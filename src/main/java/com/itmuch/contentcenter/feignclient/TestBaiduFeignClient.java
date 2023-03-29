package com.itmuch.contentcenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * feign:脱离ribbon的使用
 */
@FeignClient(name = "baidu",url = "http://www.baidu.com")
public interface TestBaiduFeignClient {

    @GetMapping("")
    public String index();
}

