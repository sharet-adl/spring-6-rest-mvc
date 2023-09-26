package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    IBeerRepository beerRepository;

    @Test
    void testSaveBeer() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                    .beerName("BeerRepo")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("3254534543")
                    .price(new BigDecimal("11.99"))
                .build());

        // very fast, context not catching it. Force flush, otherwise test might not catch ..
        // Hibernate is batching up content before persisting .. lazy write
        beerRepository.flush();

        Assertions.assertThat(savedBeer).isNotNull();
        Assertions.assertThat(savedBeer.getId()).isNotNull();
    }

    @Test
    void testSaveBeerNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
                Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("BeerRepo012345679012345679012345679012345679012345679")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("3254534543")
                .price(new BigDecimal("11.99"))
                .build());

        beerRepository.flush();
        });
    }

}