package se.magnus.microservices.core.product.recommendationservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationEntity;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    @Autowired
    private final ServiceUtil serviceUtil;

    @Autowired
    private final RecommendationRepository recommendationRepository;

    @Autowired
    private final RecommendationMapper recommendationMapper;



    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository recommendationRepository, RecommendationMapper recommendationMapper) {
        this.serviceUtil = serviceUtil;
        this.recommendationRepository = recommendationRepository;
        this.recommendationMapper = recommendationMapper;
    }


    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        List<RecommendationEntity> recommendationEntities = recommendationRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId " + productId));

        List<Recommendation> recommendations = recommendationMapper.entityListToApiList(recommendationEntities);

        recommendations.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));

        return recommendations;
    }

    @Override
    public Recommendation createRecommendations(Recommendation body) {

        try {
        RecommendationEntity entity = recommendationMapper.apiToEntity(body);


        RecommendationEntity newEntity = recommendationRepository.save(entity);

        Recommendation response = recommendationMapper.entityToApi(newEntity);

        response.setServiceAddress(serviceUtil.getServiceAddress());

        return response;

        }catch (DuplicateKeyException ex){
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
        }


    }

    @Override
    public void deleteRecommendations(int productId) {

        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        recommendationRepository.findByProductId(productId).ifPresent(
                list -> list.forEach(entity -> recommendationRepository.delete(entity)));


    }
}
