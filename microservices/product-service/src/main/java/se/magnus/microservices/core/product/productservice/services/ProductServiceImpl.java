package se.magnus.microservices.core.product.productservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.rmi.ServerError;

@RestController
public class ProductServiceImpl implements ProductService {


    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private ServiceUtil serviceUtil;
    private ProductRepository productRepository;
    private ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository productRepository, ProductMapper productMapper) {
        this.serviceUtil = serviceUtil;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product getProduct(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        Iterable<ProductEntity> b = productRepository.findAll();

        ProductEntity productEntity = productRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId " + productId));

        Product response =  productMapper.entityToApi(productEntity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        return response;
    }

    @Override
    public Product createProduct(Product body) {
        try{

            ProductEntity productEntity = productMapper.apiToEntity(body);
            ProductEntity newProductEntity = productRepository.save(productEntity);

            LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
            Product response =  productMapper.entityToApi(newProductEntity);
            response.setServiceAddress(serviceUtil.getServiceAddress());

            Iterable<ProductEntity> b = productRepository.findAll();
            return response;

        }catch (DuplicateKeyException e){
            throw new InvalidInputException("Duplicate Key, Product Id " + body.getProductId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        productRepository.findByProductId(productId).ifPresent(e -> productRepository.delete(e));
    }
}
