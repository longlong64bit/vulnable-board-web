package com.vuln.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * /uploads/* 를 업로드 디렉터리 파일로 매핑.
     * (/** 가 아니므로 /uploads, /uploads/ 는 컨트롤러에서 디렉토리 리스팅 처리)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/*")
                .addResourceLocations("file:" + path + "/");
    }
}
