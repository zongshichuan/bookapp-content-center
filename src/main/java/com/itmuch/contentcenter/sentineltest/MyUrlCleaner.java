package com.itmuch.contentcenter.sentineltest;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Sentinel扩展---restful url支持
 */
@Component
@Slf4j
public class MyUrlCleaner implements UrlCleaner {
    @Override
    public String clean(String s) {
        //让/test-rest-template-sentinel/1和/test-rest-template-sentinel/2返回值相同,都
        //返回/test-rest-template-sentinel/{number}
        String[] split = s.split("/");
        return Arrays.stream(split)
                .map(str -> {
                    if (NumberUtils.isNumber(str)) {
                        return "{number}";
                    }
                    return str;
                })
                .reduce((a, b) -> a + "/" + b)
                .orElse("");
    }
}
