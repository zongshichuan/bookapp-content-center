package com.itmuch.contentcenter.feignclient;

import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * feign--多参数构造请求
 */
@FeignClient(name = "user-center")
public interface TestUserCenterFeignClient {

    @GetMapping("/testFeignArgs")
    public UserDTO query(@SpringQueryMap UserDTO userDTO); //方法一：使用@SpringQueryMap
//    public UserDTO query(@RequestParam("id") Integer id,@RequestParam("wxId") String wxId); //方法二：有几个参数就写几个
}
