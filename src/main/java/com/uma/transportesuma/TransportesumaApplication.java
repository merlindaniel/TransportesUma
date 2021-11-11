package com.uma.transportesuma;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TransportesumaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportesumaApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(@Autowired UserService userService) {
		return args -> {
			if (false) {
				User user = new User("zodd", "zodd@gmail.com", "123");
				userService.addUser(user);
			}
		};
	}
}
