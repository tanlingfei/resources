package com.v3.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class OriginCheckUtil {

    @Autowired
    private FrontProperties frontProperties;

    /**
     * 检查请求来源是否合法
     * @param request HTTP 请求对象
     * @return true 表示来源合法，false 表示不合法
     */
    public boolean checkOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            origin = request.getHeader("Referer");
        }
        return frontProperties.isAllowedOrigin(origin);
    }

    /**
     * 检查来源是否合法，如果不合法则抛出异常
     * @param request HTTP 请求对象
     * @throws RuntimeException 当来源不合法时抛出
     */
    public void checkOriginOrThrow(HttpServletRequest request) {
        if (!checkOrigin(request)) {
            throw new RuntimeException("请求来源不合法");
        }
    }

    /**
     * 获取允许的来源列表
     */
    public java.util.List<String> getAllowedOrigins() {
        return frontProperties.getPathList();
    }
}
