package com.zj.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@EnableScheduling
@EnableEurekaClient
@SpringBootApplication(scanBasePackages = {"com.zj.client", "com.zj.common"})
public class ClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClientApplication.class);
  }
}
