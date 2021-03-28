package se.magnus.microservices.core.product.recommendationservice.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {

    Optional<List<RecommendationEntity>> findByProductId(int productId);

}
