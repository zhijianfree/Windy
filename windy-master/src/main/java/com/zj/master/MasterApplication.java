package com.zj.master;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@EnableScheduling
@MapperScan({"com.zj.domain"})
@SpringBootApplication(scanBasePackages = {"com.zj.master", "com.zj.common", "com.zj.domain"})
@EnableEurekaClient
public class MasterApplication {

  public static void main(String[] args) {
    SpringApplication.run(MasterApplication.class, args);
  }
}
