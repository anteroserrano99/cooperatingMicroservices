package se.magnus.microservices.core.product.productservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ServiceUtil serviceUtil;

    @Override
    public Product getProduct(int productId) {

        if (productId == 13){
            throw new NotFoundException("the product with id "+ productId + " has not been found");
        }


        Product product = new Product(productId,"nombre",0, serviceUtil.getServiceAddress());

        return product;
    }
}
