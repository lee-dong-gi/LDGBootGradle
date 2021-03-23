package com.web.LDGBootGradle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LdgBootGradleApplication {

	public static void main(String[] args) {
		SpringApplication.run(LdgBootGradleApplication.class, args);
	}

}
