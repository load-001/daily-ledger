package com.load.dailyledger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.load.dailyledger.mapper")
public class DailyLedgerApplication {

    public static void main(String[] args) {

        SpringApplication.run(DailyLedgerApplication.class, args);
    }

}
