package se.magnus.microservices.core.product.recommendationservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationEntity;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

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
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return recommendationRepository.findByProductId(productId)
                .log()
                .map(e -> recommendationMapper.entityToApi(e))
                .map(e ->{ e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Recommendation createRecommendations(Recommendation body) {

        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = recommendationMapper.apiToEntity(body);

        Mono<Recommendation> newEntity = recommendationRepository.save(entity)
                .log()
                .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(recommendationEntity -> {
                    Recommendation recommendation = recommendationMapper.entityToApi(recommendationEntity);
                    recommendation.setServiceAddress(serviceUtil.getServiceAddress());
                    return recommendation;
                });


        List<RecommendationEntity> r = recommendationRepository.findAll().toStream().collect(Collectors.toList());

        return newEntity.block();
    }

    @Override
    public void deleteRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

        recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId)).block();
    }
}
