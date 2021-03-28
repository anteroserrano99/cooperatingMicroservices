package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.services.ProductMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapperTests {

    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);



    @Test
    public void mapApiToEntity(){

        assertNotNull(productMapper);

        Product api = new Product(1, "name", 1, "sa");

        ProductEntity productEntity = productMapper.apiToEntity(api);

        assertEquals(api.getProductId(), productEntity.getProductId());
        assertEquals(api.getName(), productEntity.getName());
        assertEquals(api.getWeight(), productEntity.getWeight());

    }


    @Test
    public void  mapEntitytoApi(){

        assertNotNull(productMapper);

        ProductEntity productEntity = new ProductEntity(1, "name", 1);

        Product api = productMapper.entityToApi(productEntity);

        assertEquals(api.getProductId(), productEntity.getProductId());
        assertEquals(api.getName(), productEntity.getName());
        assertEquals(api.getWeight(), productEntity.getWeight());

    }






}
