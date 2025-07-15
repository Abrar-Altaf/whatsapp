package com.whatsapp.whatsapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve any file at root (e.g. /abc.jpg) from all static folders
        registry.addResourceHandler("/{filename:.+}")
            .addResourceLocations(
                "classpath:/static/profile/",
                "classpath:/static/picture/",
                "classpath:/static/video/"
            );
    }
} 