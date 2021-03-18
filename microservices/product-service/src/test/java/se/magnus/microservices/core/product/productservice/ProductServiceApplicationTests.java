package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class ProductServiceApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private WebTestClient client;


	@Test
	void getProductById() {

		int productId = 1;

		client.get()
				.uri("/product/"+ productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.productId").isEqualTo(productId);
	}



	@Test
	void getProductByIdInvalidParameter(){
		client.get()
				.uri("product/noInteger")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isBadRequest()
				.expectBody();

	}

	@Test
	public void getProductNotFound() {

		int productIdNotFound = 13;

		client.get()
				.uri("/product/" + productIdNotFound)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product/" + productIdNotFound);
	}




}
