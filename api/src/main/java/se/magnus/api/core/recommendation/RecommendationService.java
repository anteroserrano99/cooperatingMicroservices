package se.magnus.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {

    /**
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
        value    = "/recommendation",
        produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    @PostMapping(
            value    = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Recommendation createRecommendations(@RequestBody Recommendation body);


    @DeleteMapping(value    = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "productId", required = true)int productId);




}
