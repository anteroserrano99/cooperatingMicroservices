package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;

import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.product.Product;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;


@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false"})
public class ProductServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	@Autowired
    private Sink channel;

	private AbstractMessageChannel sink;

	@BeforeEach
	public void setupDb() {
		repository.deleteAll().block();
		sink = (AbstractMessageChannel) channel.input();
	}

	@Test
	public void getProductById() {

		int productId = 1;
        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long)repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long)repository.count().block());

		getAndVerifyProduct(productId, OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	public void duplicateError() {

		int productId = 1;

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());


        try {
            sendCreateProductEvent(productId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof InvalidInputException)	{
                InvalidInputException iie = (InvalidInputException)me.getCause();
                assertEquals("Duplicate key, Product Id " + productId, iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }
	}

	@Test
	public void deleteProduct() {

		int productId = 1;

		sendCreateProductEvent(productId);
		assertNotNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
	}

	@Test
	public void getProductInvalidParameterString() {

		getAndVerifyProduct("/no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getProductNotFound() {

		int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product/" + productIdNotFound);
	}

	@Test
	public void getProductInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product" + productIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateProductEvent(int productId) {
		Event<Integer, Product> event = new Event<>(CREATE, productId, new Product(productId, "Name " + productId, productId, "SA"));
		sink.send(new GenericMessage<Event>(event));
	}

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event<>(DELETE, productId, null);
        sink.send(new GenericMessage<Event>(event));
    }


}
