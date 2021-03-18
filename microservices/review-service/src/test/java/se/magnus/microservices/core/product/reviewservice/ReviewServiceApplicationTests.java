package se.magnus.microservices.core.product.reviewservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTests {

	@LocalServerPort
	private int localServerPort;

	@Autowired
	WebTestClient webTestClient;



	@Test
	void getReviewById() {
		int productId = 1;
		webTestClient
                .get()
                .uri("/review?productId="+productId)
                .exchange()
                .expectStatus().isOk();
	}


    @Test
    void getReviewByIdInvalidParameter() {
        int productId = 1;
        webTestClient
                .get()
                .uri("/review?productId=hola")
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void getReviewByIdNotFound() {
        int productId = 213;
        webTestClient
                .get()
                .uri("/review?productId="+ productId)
                .exchange()
                .expectStatus().isOk();
    }


}
