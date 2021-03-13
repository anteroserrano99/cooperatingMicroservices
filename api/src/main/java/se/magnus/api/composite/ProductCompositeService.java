package se.magnus.api.composite;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.magnus.api.core.product.Product;

public interface ProductCompositeService {

    @GetMapping(
            value = "product-composite/{productId}",
            produces = "application/json"
    )
     ProductAggregate getproduct(@PathVariable int productId);

}
