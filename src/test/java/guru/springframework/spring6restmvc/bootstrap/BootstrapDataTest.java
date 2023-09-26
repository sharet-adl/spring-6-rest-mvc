package guru.springframework.spring6restmvc.bootstrap;

import guru.springframework.spring6restmvc.repositories.IBeerRepository;
import guru.springframework.spring6restmvc.repositories.ICustomerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BootstrapDataTest {
    @Autowired
    IBeerRepository beerRepository;

    @Autowired
    ICustomerRepository customerRepository;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, customerRepository);
    }

    @Test
    void run() throws Exception {
        bootstrapData.run(null);

        Assertions.assertThat(beerRepository.count()).isEqualTo(3);
        Assertions.assertThat(customerRepository.count()).isEqualTo(2);
    }
}