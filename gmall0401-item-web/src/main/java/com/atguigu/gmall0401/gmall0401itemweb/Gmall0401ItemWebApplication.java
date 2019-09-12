package com.atguigu.gmall0401.gmall0401itemweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0401") // Redis用
public class Gmall0401ItemWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401ItemWebApplication.class, args);
	}

}
