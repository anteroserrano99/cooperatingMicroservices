package se.magnus.microservices.core.product.recommendationservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationServiceApplicationTests {


	@LocalServerPort
	private int port;

	@Autowired
	WebTestClient webTestClient;


	@Test
	void getRecommendationById() {

		int productId = 1;
		webTestClient
				.get()
				.uri("/recommendation?productId="+productId)
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	void getRecommendationInvalidParameter(){


		webTestClient
				.get()
				.uri("/recommendation?productId=hola")
				.exchange()
				.expectStatus().isBadRequest();


	}

	@Test
	void getRecommendationNotFound(){

		int productId = 113;
		webTestClient
				.get()
				.uri("/recommendation?productId="+productId)
				.exchange()
				.expectStatus().isOk();


	}



}
