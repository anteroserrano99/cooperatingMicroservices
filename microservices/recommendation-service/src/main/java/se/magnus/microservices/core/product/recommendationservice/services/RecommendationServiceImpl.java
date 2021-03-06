package se.magnus.microservices.core.product.recommendationservice.services;

import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;

import java.util.List;

public class RecommendationServiceImpl implements RecommendationService {
    @Override
    public List<Recommendation> getRecommendations(int productId) {
        return null;
    }
}
