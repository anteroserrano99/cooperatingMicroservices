package se.magnus.microservices.core.product.productservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.rmi.ServerError;
import java.util.Random;

@RestController
public class ProductServiceImpl implements ProductService {


    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ServiceUtil serviceUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;


    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {

        LOG.debug("Product is been search with productId" + productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        if (delay > 0) simulateDelay(delay);

        if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

        return  productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product Found with Id: "+ productId)))
                .log()
                .map(e -> productMapper.entityToApi(e))
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public Product createProduct(Product body) {

            ProductEntity productEntity = productMapper.apiToEntity(body);
            Mono<Product> newProductEntity = productRepository.save(productEntity)
                    .log()
                    .onErrorMap(DuplicateKeyException.class,
                            ex -> new InvalidInputException("Duplicate key, Product Id " + body.getProductId()))
                    .map(e -> productMapper.entityToApi(productEntity));

            return  newProductEntity.block();

    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        productRepository.findByProductId(productId)
            .log()
            .map(e -> productRepository.delete(e)).flatMap(e -> e).block();

    }



    private void simulateDelay(int delay) {
        LOG.debug("Sleeping for {} seconds...", delay);
        try {Thread.sleep(delay * 1000);} catch (InterruptedException e) {}
        LOG.debug("Moving on...");
    }

    private void throwErrorIfBadLuck(int faultPercent) {
        int randomThreshold = getRandomNumber(1, 100);
        if (faultPercent < randomThreshold) {
            LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
        } else {
            LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
            throw new RuntimeException("Something went wrong...");
        }
    }

    private final Random randomNumberGenerator = new Random();
    private int getRandomNumber(int min, int max) {

        if (max < min) {
            throw new RuntimeException("Max must be greater than min");
        }

        return randomNumberGenerator.nextInt((max - min) + 1) + min;
    }
}
