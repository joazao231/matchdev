package br.com.joao.matchdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MatchDevApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchDevApplication.class, args);
	}

}
