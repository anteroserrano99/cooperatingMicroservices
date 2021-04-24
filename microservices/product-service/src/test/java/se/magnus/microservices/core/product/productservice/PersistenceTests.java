package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {


    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;


    @BeforeEach
    public void setupDb(){
        repository.deleteAll().block();
        ProductEntity entity = new ProductEntity(1, "n", 1);
        StepVerifier.create(repository.save(entity))
            .expectNextMatches(createdEntity ->
                    {
                     savedEntity = createdEntity;
                     return savedEntity.getProductId() == entity.getProductId();
                    })
            .verifyComplete();

    }


    @Test
    public void create(){
        ProductEntity newEntity = new ProductEntity(2, "n", 1);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getProductId() == foundEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
        
    }



    @Test
    public void update(){
        savedEntity.setName("n2");
        repository.save(savedEntity).block();

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).block();

        assertEquals(1, (long) foundEntity.getVersion() );

        assertEquals("n2", foundEntity.getName());
    }


    @Test
    public void getProductById(){
        ProductEntity productEntity = repository.findByProductId(savedEntity.getProductId()).block();
        assertTrue(productEntity != null);
        assertEquals(productEntity.getId(), savedEntity.getId());
    }


    @Test
    public void duplicateKeyError(){
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }



    @Test
    public void optimisticLockError(){

        ProductEntity productEntity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity productEntity2 = repository.findById(savedEntity.getId()).block();

        productEntity1.setName("n1");
        repository.save(productEntity1).block();

        productEntity2.setName("n2");
        StepVerifier.create(repository.save(productEntity2)).expectError(OptimisticLockingFailureException.class).verify();

        StepVerifier.create(repository.findById(productEntity1.getId()))
            .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n1"))
            .verifyComplete();
    }


    @Test
    public void delete(){
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

}
