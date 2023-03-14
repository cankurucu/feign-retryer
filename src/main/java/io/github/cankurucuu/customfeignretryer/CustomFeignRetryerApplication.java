package io.github.cankurucuu.customfeignretryer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CustomFeignRetryerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomFeignRetryerApplication.class, args);
    }

}
