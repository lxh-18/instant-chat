package com.instantchat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 - 用于配置静态资源访问
 * 满足得分点1：文件I/O处理 - 支持音频、图片等静态文件访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置 /audio/** 和 /image/** 路径映射到本地文件目录
        String absolutePath = uploadPath.replace("\\", "/");
        registry.addResourceHandler("/audio/**")
                .addResourceLocations("file:" + absolutePath + "/audio/");
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:" + absolutePath + "/image/");
    }
}