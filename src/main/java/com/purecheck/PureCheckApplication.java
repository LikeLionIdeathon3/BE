package com.purecheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 스프링 부트 애플리케이션의 시작점입니다.
// 이 클래스를 실행하면 서버가 8080 포트에서 시작됩니다.
@SpringBootApplication
public class PureCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(PureCheckApplication.class, args);
    }
}
