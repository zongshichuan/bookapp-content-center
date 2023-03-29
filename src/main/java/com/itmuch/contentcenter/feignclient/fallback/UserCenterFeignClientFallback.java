package com.itmuch.contentcenter.feignclient.fallback;

import com.itmuch.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserCenterFeignClient：的fallback的处理类
 *
 */
@Component
@Slf4j
public class UserCenterFeignClientFallback implements UserCenterFeignClient {
    @Override
    public UserDTO findById(Integer id,String token) {
        log.warn("findById远程调用被限流/降级了,返回默认用户!");
        UserDTO userDTO = new UserDTO();
        userDTO.setWxNickname("流控/降级的default user");
        return userDTO;
    }

    @Override
    public UserDTO addBonus(UserAddBonusDTO userAddBonusDTO) {
        log.warn("addBonus远程调用被限流/降级了,返回默认用户!");
        return UserDTO.builder()
                .wxNickname("流控/降级的default user")
                .build();
    }
}
