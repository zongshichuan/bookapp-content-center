package com.itmuch.contentcenter.sentineltest;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sentinel扩展--错误页优化
 */
@Component
public class MyUrlBlockHandler implements UrlBlockHandler {

    @Override
    public void blocked(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse, BlockException e) throws IOException {
        ErrorMsg msg = null;

        //1、限流规则异常
        if (e instanceof FlowException){
            msg = ErrorMsg.builder()
                    .status(100)
                    .msg("限流")
                    .build();
        }
        //2、降级规则异常
        else if(e instanceof DegradeException){
            msg = ErrorMsg.builder()
                    .status(101)
                    .msg("降级")
                    .build();
        }
        //3、参数热点规则异常
        else if (e instanceof ParamFlowException){
            msg = ErrorMsg.builder()
                    .status(102)
                    .msg("热点参数限流")
                    .build();
        }
        //4、系统规则异常
        else if (e instanceof SystemBlockException){
            msg = ErrorMsg.builder()
                    .status(103)
                    .msg("系统规则(负载/..不满足要求)")
                    .build();
        }
        //5、授权规则异常
        else if (e instanceof AuthorityException){
            msg = ErrorMsg.builder()
                    .status(104)
                    .msg("授权规则不通过")
                    .build();
        }

        //http状态码
        httpServletResponse.setStatus(500);
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
        httpServletResponse.setContentType("application/json;charset=utf-8");

        // spring mvc自带的json操作工具，叫jackson
        new ObjectMapper()
                .writeValue(
                        httpServletResponse.getWriter(),
                        msg
                );
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ErrorMsg{
    private Integer status;
    private String msg;
}