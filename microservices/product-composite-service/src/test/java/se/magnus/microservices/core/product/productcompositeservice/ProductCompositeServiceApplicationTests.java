package se.magnus.microservices.core.product.productcompositeservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.microservices.core.product.productcompositeservice.services.ProductCompositeIntegration;

import java.util.ArrayList;
import java.util.List;

//import static org.mockito.;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	@LocalServerPort
	private int localport;


	@Autowired
	WebTestClient webTestClient;

	@MockBean
	ProductCompositeIntegration productCompositeIntegration;


	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;


	@BeforeEach
	void setUp() {

		List<Recommendation> recommendations = new ArrayList<>();
		recommendations.add(new Recommendation(1, 1, "Author 1", 1, "Content 1", "mockAdress"));
		recommendations.add(new Recommendation(1, 2, "Author 2", 2, "Content 2", "mockAdress"));
		recommendations.add(new Recommendation(1, 3, "Author 3", 3, "Content 3", "mockAdress"));

		List<Review> reviews = new ArrayList<>();
		reviews.add(new Review(1, 1, "Author 1", "Subject 1", "Content 1", "mockAdress"));
		reviews.add(new Review(1, 2, "Author 2", "Subject 2", "Content 2", "mockAdress"));
		reviews.add(new Review(1, 3, "Author 3", "Subject 3", "Content 3", "mockAdress"));


		when(productCompositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(1, "test", 1, "mockAdress"));
		when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(recommendations);
		when(productCompositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(reviews);




	}

	@Test
	void getProductCompositeById(){
		//product-composite/{productId}

		webTestClient
				.get()
				.uri("/product-composite/" + PRODUCT_ID_OK)
				.exchange()
				.expectStatus().isOk();
	}





}
