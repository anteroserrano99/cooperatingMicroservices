package se.magnus.microservices.core.product.productcompositeservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.composite.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    @Autowired
    ServiceUtil serviceUtil;

    @Autowired
    ProductCompositeIntegration integration;


    public ProductCompositeServiceImpl (ServiceUtil serviceUtil,
        ProductCompositeIntegration integration){

        this.integration = integration;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public ProductAggregate getproduct(int productId) {
        Product product = integration.getProduct(productId);

        if (product == null) throw new NotFoundException("No product found for productId "+ productId);

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        return createAggreateProduct(product, reviews, recommendations, serviceUtil.getServiceAddress());
    }


    public ProductAggregate createAggreateProduct(Product product, List<Review> reviews, List<Recommendation> recommendations, String serviceAddress){
        int productId = product.getProductId();
        String productName = product.getName();
        int productWeight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map( r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                    .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                    .collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "" ;
        String reccommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, reccommendationAddress);

        return new ProductAggregate(productId, productName, productWeight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }




}
