package se.magnus.microservices.core.product.recommendationservice;

import org.awaitility.Awaitility;
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
import reactor.core.publisher.Flux;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationEntity;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
public class RecommendationServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@Autowired
    private Sink sink;

	private AbstractMessageChannel channel;


	@BeforeEach
	public void setupDb() {
		repository.deleteAll().block();
		channel = (AbstractMessageChannel) sink.input();
	}

	@Test
	public void getRecommendationsByProductId() {

		int productId = 1;

		sendCreateRecommendationEvent(productId, 1);
		sendCreateRecommendationEvent(productId, 2);
		sendCreateRecommendationEvent(productId, 3);

		assertEquals(3, repository.findByProductId(productId).count().block());

		getAndVerifyRecommendationsByProductId(productId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].productId").isEqualTo(productId)
			.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);

		assertEquals(1, (long)repository.count().block());


		try {
			sendCreateRecommendationEvent(productId, recommendationId);

			List<RecommendationEntity> b = repository.findAll().toStream().collect(Collectors.toList());
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}


	@Test
	public void deleteRecommendations() {

		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);
		assertEquals(1, repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
		assertEquals(0, repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
	}

	@Test
	public void getRecommendationsMissingParameter() {

		getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	public void getRecommendationsInvalidParameter() {

		getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRecommendationsNotFound() {

		getAndVerifyRecommendationsByProductId("?productId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRecommendationsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/recommendation" + productIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateRecommendationEvent(int productId, int recommendationId) {
            Event<Integer, Recommendation> event = new Event<Integer, Recommendation>(CREATE, productId,  new Recommendation(productId, recommendationId, "", 0, "", null));
	        channel.send(new GenericMessage<Event>(event));
	}

    private void sendDeleteRecommendationEvent(int productId) {
        Event<Integer, Recommendation> event = new Event(DELETE, productId,  null);
        channel.send(new GenericMessage<Event>(event));
    }

}
