package se.magnus.microservices.core.product.recommendationservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {

    Flux<RecommendationEntity> findByProductId(int productId);

}
