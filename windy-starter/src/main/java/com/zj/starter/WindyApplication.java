package com.zj.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author falcon
 * @since 2023/1/28
 */
@ComponentScan("com.zj")
@MapperScan("com.zj.*.mapper")
@SpringBootApplication
public class WindyApplication {

  public static void main(String[] args) {
    SpringApplication.run(WindyApplication.class, args);
  }

}
