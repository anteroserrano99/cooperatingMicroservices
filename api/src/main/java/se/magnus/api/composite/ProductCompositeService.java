package se.magnus.api.composite;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.magnus.api.core.product.Product;

@Api()
public interface ProductCompositeService {

    @ApiOperation(
            value = "${api.product-composite.get-composite-product.description}",
            notes = "${api.product-composite.get-composite-product.notes}"
    )
    @ApiResponses( value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format"),
            @ApiResponse(code = 200, message = "Request is ok"),
            @ApiResponse(code = 404, message = "Product not found")
    })
    @GetMapping(
            value = "product-composite/{productId}",
            produces = "application/json"
    )
     ProductAggregate getproduct(@PathVariable int productId);

}
