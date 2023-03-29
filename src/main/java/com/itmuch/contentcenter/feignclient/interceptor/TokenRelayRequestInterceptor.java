package com.itmuch.contentcenter.feignclient.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *Feign的拦截器RequestInterceptor
 *
 * 该RequestInterceptor接口允许您拦截由Feign客户端生成的传出HTTP请求，并根据需要修改其标头、正文或者URL
 *
 * Spring cloud的微服务使用Feign进行服务间调用的时候可以使用RequestInterceptor统一拦截请求来完成设置header等相关请求，
 * 但是RequestInterceptor和ClientHttpRequestInterceptor有点不同，它拿不到原本的请求，所以要通过其他方法来获取原本的请求。
 */
@Slf4j
public class TokenRelayRequestInterceptor implements RequestInterceptor {

    /**
     * 该方法修改请求模板的地方
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //1、获取到token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        log.info("feign的拦截器，拦截请求:{}",request);
        String token = request.getHeader("x-token");
        //2、将token传递
        if (StringUtils.isNotBlank(token)){
            requestTemplate.header("x-token",token);
        }
    }
}
