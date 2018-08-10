package com.atguigu.gmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall")
@MapperScan(basePackages = "com.atguigu.gmall.payment.mapper")
public class GmallPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPaymentApplication.class, args);
	}
}
