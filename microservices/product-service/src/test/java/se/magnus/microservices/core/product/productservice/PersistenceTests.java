package se.magnus.microservices.core.product.productservice;

import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import se.magnus.microservices.core.product.productservice.persistence.ProductEntity;
import se.magnus.microservices.core.product.productservice.persistence.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.LongStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;


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



    @Test
    public void update(){
        savedEntity.setName("n2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();

        assertEquals(1, (long) foundEntity.getVersion() );

        assertEquals("n2", foundEntity.getName());
    }


    @Test
    public void getProductById(){
        Optional<ProductEntity> productEntity = repository.findByProductId(savedEntity.getProductId());
        assertTrue(productEntity.isPresent());
        assertEquals(productEntity.get().getId(), savedEntity.getId());
    }


    @Test

    public void duplicateKeyError(){
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);

        assertThrows(DuplicateKeyException.class, () -> {
        repository.save(entity);

        });

        assertEquals(1,repository.count());
    }








    @Test
    public void optimisticLockError(){

        ProductEntity productEntity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity productEntity2 = repository.findById(savedEntity.getId()).get();

        productEntity1.setName("n1");
        repository.save(productEntity1);

        try{
            productEntity2.setName("n2");
            repository.save(productEntity2);
            fail("Expected optimistic Lock");
        }catch (OptimisticLockingFailureException e){}

        ProductEntity foundEntity = repository.findById(productEntity1.getId()).get();

        assertEquals(1, foundEntity.getVersion());
        assertEquals("n1", foundEntity.getName());




    }


    @Test
    public void delete(){

        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }



    @Test
    public void paging(){
        repository.deleteAll();

        List<ProductEntity> productEntities = rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity((int)i, "name " + i ,(int) i))
                .collect(Collectors.toList());
        repository.saveAll(productEntities);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }


    private Pageable testNextPage (Pageable nextPage, String expectedProductIds, boolean expectsNextPage ){

        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent()
                .stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());

        assertEquals(expectsNextPage, productPage.hasNext());

        return productPage.nextPageable();


    }




}
