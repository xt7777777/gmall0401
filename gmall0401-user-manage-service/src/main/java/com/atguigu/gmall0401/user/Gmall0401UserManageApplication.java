package com.atguigu.gmall0401.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0401.user.mapper")
public class Gmall0401UserManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401UserManageApplication.class, args);
	}

}
