package com.v3.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "front")
public class FrontProperties {
    private String path;

    /**
     * 获取允许的前端路径列表（支持逗号分隔）
     */
    public List<String> getPathList() {
        if (!StringUtils.hasText(path)) {
            return Collections.emptyList();
        }
        return Arrays.asList(path.split(","));
    }

    /**
     * 检查来源是否在允许的列表中
     */
    public boolean isAllowedOrigin(String origin) {
        if (!StringUtils.hasText(origin)) {
            return false;
        }
     /*   System.out.println("请求来源===="+origin);*/
        List<String> pathList = getPathList();
        for (String allowedPath : pathList) {
            if (origin.trim().startsWith(allowedPath.trim())) {
                return true;
            }
        }
        return false;
    }
}
