package com.purecheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// RestTemplate: 외부 API(식약처)를 호출할 때 사용하는 HTTP 클라이언트입니다.
// @Bean으로 등록해두면 다른 클래스에서 @Autowired로 가져다 쓸 수 있습니다.
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
