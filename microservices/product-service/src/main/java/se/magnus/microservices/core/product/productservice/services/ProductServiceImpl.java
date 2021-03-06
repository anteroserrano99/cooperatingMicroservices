package se.magnus.microservices.core.product.productservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ServiceUtil serviceUtil;

    @Override
    public Product getProduct(int productId) {

        Product product = new Product(productId,"nombre",0, serviceUtil.getServiceAddress());

        return product;
    }
}
