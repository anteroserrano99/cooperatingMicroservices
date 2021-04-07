package se.magnus.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
        value    = "/review",
        produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);


    @PostMapping(
            value    = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review body);


    @DeleteMapping(value    = "/review")
    void deleteReviews(@RequestParam(value = "productId", required = true) int productId);

}