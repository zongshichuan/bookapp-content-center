package com.itmuch.contentcenter.restinterceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * restTemplate拦截器
 *
 * Spring提供了ClientHttpRequestInterceptor接口，可以对请求进行拦截，
 * 并在其被发送至服务端之前添加自定义标头、修改请求正文或替换请求 URL
 */
@Slf4j
public class TestRestTemplateTokenRelayInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("x-token");

        HttpHeaders headers = httpRequest.getHeaders();
        headers.add("x-token",token);
        //保证请求继续执行
        return clientHttpRequestExecution.execute(httpRequest,body);
    }
}
