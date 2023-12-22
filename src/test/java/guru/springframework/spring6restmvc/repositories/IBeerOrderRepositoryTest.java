package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
@SpringBootTest
class IBeerOrderRepositoryTest {
    @Autowired
    IBeerOrderRepository beerOrderRepository;

    @Autowired
    ICustomerRepository customerRepository;

    @Autowired
    IBeerRepository beerRepository;

    Customer testCustomer;
    Beer testBeer;

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.findAll().get(0);
        testBeer = beerRepository.findAll().get(0);
    }

    @Transactional
    @Test
    void testBeerOrders() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Test order")
                .customer(testCustomer)
                .beerOrderShipment(BeerOrderShipment.builder()
                        .trackingNumber("12345")
                        .build())
                .build();

        // avoid working with original object (beerOrder), it is not guaranteed it is updated .. use the savedBeerOrder
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        // LazyInitializationException. saveAndFlush() WA could add performance degradation
        //BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);


        System.out.println(savedBeerOrder.getCustomerRef());
    }
}