package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;



@ExtendWith(SpringExtension.class)
@DataMongoTest
public class PersistenceTests {


    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;


    @BeforeEach
    public void setupDb(){
        repository.deleteAll();
        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);
        assertEquals(entity, savedEntity);
    }


    @Test
    public void create(){
        ProductEntity newEntity = new ProductEntity(2, "n", 1);

        savedEntity = repository.save(newEntity);
        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();

        assertEquals(2, repository.count());


    }



}
