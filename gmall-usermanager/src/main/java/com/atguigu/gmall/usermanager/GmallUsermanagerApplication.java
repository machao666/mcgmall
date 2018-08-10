package com.atguigu.gmall.usermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.usermanager.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall")
public class GmallUsermanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanagerApplication.class, args);
	}
}
