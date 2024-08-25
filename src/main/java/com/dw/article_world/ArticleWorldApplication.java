package com.dw.article_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ArticleWorldApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleWorldApplication.class, args);
    }

}
