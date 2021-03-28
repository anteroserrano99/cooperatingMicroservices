package se.magnus.microservices.core.product.recommendationservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationEntity;
import se.magnus.microservices.core.product.recommendationservice.services.RecommendationMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MapperTests {


    @Autowired
    RecommendationMapper recommendationMapper;



    @Test
    public void apiToEntityTest(){

        assertNotNull(recommendationMapper);

        Recommendation api = new Recommendation(1, 1, "a", 1, "c", null);

        RecommendationEntity entity =recommendationMapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());
    }


    @Test
    public void EntityToApiTest(){


        assertNotNull(recommendationMapper);

        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");

        Recommendation api = recommendationMapper.entityToApi(entity);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());

    }

}
