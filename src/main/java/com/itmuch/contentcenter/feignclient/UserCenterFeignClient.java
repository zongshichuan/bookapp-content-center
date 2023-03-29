package com.itmuch.contentcenter.feignclient;

import com.itmuch.contentcenter.configuration.UserCenterFeignConfiguration;
import com.itmuch.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.fallback.UserCenterFeignClientFallback;
import com.itmuch.contentcenter.feignclient.fallbackfactory.UserCenterFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * feign--使用自定义配置使用
 */
//@FeignClient(name = "user-center",configuration = UserCenterFeignConfiguration.class)
//@FeignClient(name = "user-center",fallback = UserCenterFeignClientFallback.class)
@FeignClient(name = "user-center",fallbackFactory = UserCenterFeignClientFallbackFactory.class)
public interface UserCenterFeignClient {
    /**
     * feign的功能
     *
     * 1、feign会构造出：http://user-center/users/{id}的url
     * 2、负载均衡
     * 3、把得到的结果转换为UserDTO
     * @param id
     * @return
     */
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable Integer id,
                     @RequestHeader("x-token") String token);

    @PutMapping("/users/add-bonus")
    UserDTO addBonus(@RequestBody UserAddBonusDTO userAddBonusDTO);
}
