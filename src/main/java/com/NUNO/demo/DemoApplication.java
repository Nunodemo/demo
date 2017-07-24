package com.NUNO.demo;

import com.NUNO.demo.service.ProductService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext app = SpringApplication.run(DemoApplication.class, args);

        ProductService productService = (ProductService) app.getBean("productService");
        productService.populateDbFromResource();
        System.out.println("Application is ready");
    }
}
