package com.koramarket.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.koramarket.order.model")
@EnableJpaRepositories(basePackages = "com.koramarket.order.repository")
//@ComponentScan(basePackages = {"com.koramarket.order", "com.koramarket.common"})
@SpringBootApplication(scanBasePackages = {"com.koramarket"})
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
