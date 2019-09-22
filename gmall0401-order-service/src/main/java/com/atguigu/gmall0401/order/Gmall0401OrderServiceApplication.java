package com.atguigu.gmall0401.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0401.order.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall0401")
@EnableTransactionManagement
public class Gmall0401OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401OrderServiceApplication.class, args);
	}

}
