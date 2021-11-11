package com.uma.transportesuma;

import com.fasterxml.jackson.databind.JsonNode;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TransportesumaApplication {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;

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

			// Insert random users (https://randomuser.me/api/)
			if (false) {
				try {
					int N_USERS = 20;
					for (int i = 0; i < N_USERS; i++) {
						HttpEntity<JsonNode> entity = null;
						ResponseEntity<JsonNode> randomUser = restTemplate.exchange("https://randomuser.me/api/", HttpMethod.GET, entity, JsonNode.class);
						JsonNode map = randomUser.getBody().get("results").get(0);

						User user = new User(map.get("login").get("username").asText(), map.get("email").asText(), map.get("login").get("password").asText());

						userService.addUser(user);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}
}
