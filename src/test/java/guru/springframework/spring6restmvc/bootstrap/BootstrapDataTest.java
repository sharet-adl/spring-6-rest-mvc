package guru.springframework.spring6restmvc.bootstrap;

import guru.springframework.spring6restmvc.model.BeerCSVRecord;
import guru.springframework.spring6restmvc.repositories.IBeerRepository;
import guru.springframework.spring6restmvc.repositories.ICustomerRepository;
import guru.springframework.spring6restmvc.service.BeerCsvService;
import guru.springframework.spring6restmvc.service.IBeerCsvService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BeerCsvService.class)  // Bring explicitly extra dependency, into limited (test-splice) context
class BootstrapDataTest {
    @Autowired
    IBeerRepository beerRepository;

    @Autowired
    ICustomerRepository customerRepository;

    @Autowired
    IBeerCsvService beerCsvService;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, customerRepository, beerCsvService);
    }

    @Test
    void run() throws Exception {
        bootstrapData.run(null);

        // 2410 - nr entries in CSV
        Assertions.assertThat(beerRepository.count()).isEqualTo(3 + 2410);
        Assertions.assertThat(customerRepository.count()).isEqualTo(2);
    }
}