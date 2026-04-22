package com.v3.system.config;

import com.v3.common.utils.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Autowired
    private OssProperties ossProperties;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:"+ossProperties.getLocPath());

        // 添加 CSS 静态资源映射
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        /*registry.addResourceHandler("/temp/**").addResourceLocations("classpath:/temp/").resourceChain(true);*/
    }
}
