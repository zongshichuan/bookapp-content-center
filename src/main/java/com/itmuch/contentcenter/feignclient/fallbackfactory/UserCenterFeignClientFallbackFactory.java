package com.itmuch.contentcenter.feignclient.fallbackfactory;

import com.itmuch.contentcenter.domain.dto.user.UserAddBonusDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserCenterFeignClient：的fallbackfactory的处理类
 */
@Component
@Slf4j
public class UserCenterFeignClientFallbackFactory
        implements FallbackFactory<UserCenterFeignClient> {
    @Override
    public UserCenterFeignClient create(Throwable throwable) {
        return new UserCenterFeignClient() {
            @Override
            public UserDTO findById(Integer id,String token) {
                log.warn("远程调用异常",throwable);
                UserDTO userDTO = new UserDTO();
                userDTO.setWxNickname("default user");
                return userDTO;
            }

            @Override
            public UserDTO addBonus(UserAddBonusDTO userAddBonusDTO) {
                log.warn("addBonus远程调用异常",throwable);
                return null;
            }
        };
    }
}
