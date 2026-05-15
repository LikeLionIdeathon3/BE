package com.purecheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS 설정: 프론트엔드(localhost:3000)에서 백엔드(localhost:8080)로
// 요청을 보낼 수 있도록 허용합니다.
// 브라우저는 기본적으로 다른 포트 간의 요청을 막기 때문에 이 설정이 필요합니다.
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")          // /api/ 로 시작하는 모든 경로에 적용
                        .allowedOrigins("http://localhost:3000")  // 프론트엔드 주소 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
