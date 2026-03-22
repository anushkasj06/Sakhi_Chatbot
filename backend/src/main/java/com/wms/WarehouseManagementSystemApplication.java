package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WarehouseManagementSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(WarehouseManagementSystemApplication.class, args);
  }
}
