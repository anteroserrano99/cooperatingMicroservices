package se.magnus.microservices.core.product.recommendationservice.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.product.recommendationservice.persistence.RecommendationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {


    @Mappings({
            @Mapping(target = "rating", source = "api.rate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            })
    RecommendationEntity apiToEntity(Recommendation api);

    @Mappings({
            @Mapping(target = "rate", source="entity.rating"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation entityToApi(RecommendationEntity entity);



    List<Recommendation> entityListToApiList(List<RecommendationEntity> entity);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> api);


}
