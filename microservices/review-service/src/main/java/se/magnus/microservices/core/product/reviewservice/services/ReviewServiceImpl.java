package se.magnus.microservices.core.product.reviewservice.services;

import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    @Override
    public List<Review> getReviews(int productId) {
        return new Review();
    }
}
