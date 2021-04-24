package se.magnus.microservices.core.product.productcompositeservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {


    private Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final SecurityContext nullSC = new SecurityContextImpl();

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
    public Mono<Void> createCompositeProduct(ProductAggregate body) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositeProduct(sc, body)).then();
    }

    public void internalCreateCompositeProduct(SecurityContext sc, ProductAggregate body) {

        logAuthorizationInfo(sc);

        try {
            Product productObject = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(productObject);
            if (body.getRecommendations() != null){
                body.getRecommendations().forEach(r ->{

                    Recommendation recommendation = new Recommendation(body.getProductId(),
                            r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null
                            );
                    integration.createRecommendations(recommendation);
                } );

                if (body.getReviews() != null){
                    body.getReviews().forEach(r ->{
                        Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);

                        integration.createReview(review);
                    });
                }

            }
        }catch (RuntimeException ex){
            throw ex;
        }

    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId, int delay, int faultPercent) {

        return Mono.zip(
                values -> createAggreateProduct((SecurityContext) values[0],(Product) values[1], (List<Recommendation>) values[2], (List<Review>) values[3], serviceUtil.getServiceAddress() ),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getProduct(productId, delay, faultPercent),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();

    }

    @Override
    public Mono<Void> deleteCompositeProduct(int productId) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositeProduct(sc, productId)).then();
    }



    public void internalDeleteCompositeProduct(SecurityContext sc, int productId) {

        try {
        logAuthorizationInfo(sc);

        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);
        integration.deleteProduct(productId);

        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);

    } catch (RuntimeException re) {
        LOG.warn("deleteCompositeProduct failed: {}", re.toString());
        throw re;
    }
    }


    public ProductAggregate createAggreateProduct(SecurityContext sc, Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress){

        logAuthorizationInfo(sc);

        int productId = product.getProductId();
        String productName = product.getName();
        int productWeight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map( r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                    .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                    .collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "" ;
        String reccommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, reccommendationAddress);

        return new ProductAggregate(productId, productName, productWeight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }


}
