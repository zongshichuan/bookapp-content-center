package com.itmuch.contentcenter.auth;

import com.itmuch.contentcenter.util.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthAspect {

    private final JwtOperator jwtOperator;

    //所有加了CheckLogin注解的方法都会走到这里
    @Around("@annotation(com.itmuch.contentcenter.auth.CheckAuthorization)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            //1、验证token是否合法
            checkLogin();
            //2、验证角色是否满足权限
            HttpServletRequest request = getHttpServletRequest();
            String role = (String) request.getAttribute("role");

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            CheckAuthorization annotation = method.getAnnotation(CheckAuthorization.class);
            String value = annotation.value();
            if (!Objects.equals(role,value)){
                throw new SecurityException("用户无权访问!");
            }
        } catch (Throwable throwable) {
            throw new SecurityException("用户无权访问!",throwable);
        }
        return joinPoint.proceed();
    }

    public void checkLogin(){
        try {
            //1、从header中获取token
            HttpServletRequest request = getHttpServletRequest();

            String token = request.getHeader("x-token");
            //2、校验token是否合法
            Boolean isValid = this.jwtOperator.validateToken(token);
            if (!isValid){
                throw new SecurityException("token不合法!");
            }

            //3、校验成功，将用户的信息设置到request的attribute里面
            Claims claims = jwtOperator.getClaimsFromToken(token);
            request.setAttribute("id",claims.get("id"));
            request.setAttribute("wxNickName",claims.get("wxNickName"));
            request.setAttribute("role",claims.get("role"));

        } catch (Throwable throwable) {
            throw new SecurityException("token不合法!");
        }
    }

    public HttpServletRequest getHttpServletRequest(){
        //1、从header中获取token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        return request;
    }
}
