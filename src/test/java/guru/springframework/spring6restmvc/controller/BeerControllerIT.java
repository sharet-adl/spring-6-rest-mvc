package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.IBeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.IBeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// old @WebMvcTest - not full context, see below
@SpringBootTest
class BeerControllerIT {
    @Autowired
    BeerController beerController;

    @Autowired
    IBeerRepository beerRepository;

    // only for few extra tests, to help on conversions
    @Autowired
    IBeerMapper beerMapper;


    // So far, worked directly with the Controller, but JPA validation violations will bubble up differently
    // test with JPA layer, before was using Mockito
    // We can manually build and set MockMVC env, using the WAC =>Spring Data repositories injected into service, so full Spring Boot test
    // can add test for Patch operation failure ..
    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Autowired
    ObjectMapper objectMapper;



    @Test
    void testListBeers() {
        Page<BeerDTO> dtos = beerController.listBeers(null, null, null, 1, 1000);

        // we had set a hardcodwed limit of max 1000 records, otherwise should return: 3 + 2410
        assertThat(dtos.getContent().size()).isEqualTo(1000);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {
        beerRepository.deleteAll();
        Page<BeerDTO> dtos = beerController.listBeers(null, null, null, 1, 25);
        assertThat(dtos.getContent().size()).isEqualTo(0);
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void saveNewBeerTest() {
        BeerDTO dto = BeerDTO.builder()
                .beerName("TestSameNew-Beer")
                .build();

        // emulate what Spring would do - parse json, create DTO, Spring MVC would invoke our controller method with dto
        ResponseEntity responseEntity = beerController.handlePost(dto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepository.findAll().get(1);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED BEER";
        beerDTO.setBeerName(beerName);

        ResponseEntity responseEntity = beerController.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().get(0);
        ResponseEntity responseEntity = beerController.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(beerRepository.findById(beer.getId()).isEmpty());
    }

    @Test
    void deleteByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.deleteById(UUID.randomUUID());
        });

    }

    @Test
    void testPatchBeerBadName() throws Exception {
        // Without any error handling, the validation constraint from JPA ( name len > 50 chars ) will
        // bubble up as org.sf.transaction.TransactionSystemException and transaction will be auto-rolledback
        Beer beer = beerRepository.findAll().get(0);

        // set some adhoc JSON for testing
        // not sending fully qualified object, but only the modified properties
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New too-long Name01234567890123456789012345678901234567890123456789");

        MvcResult result = mockMvc.perform(patch("/api/v1/beer/" + beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void testListBeersByName() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.content.size()", is(336)));
                .andExpect(jsonPath("$.content.size()", is(25)));
    }

    @Test
    void testListBeersByStyle() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageSize", "800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(547)));
                //.andExpect(jsonPath("$.content.size()", is(25)));
    }

    @Test
    void testListBeersByNameAndStyle() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.size()", is(310)));
                .andExpect(jsonPath("$.content.size()", is(25)));
    }

    @Test
    void testListBeersByNameAndStyleShowInventoryFalse() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "false"))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.content.size()", is(25)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void testListBeersByNameAndStyleShowInventoryTrue() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true"))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content.size()", is(25)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByNameAndStyleShowInventoryTruePage2() throws Exception {
        // could use Mocks, but easier to use IT context
        mockMvc.perform(get(BeerController.BASE_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Disabled // just for demo purposes - OptimisticLocking - ObjectOptimisticLockingFailureException
    @Test
    void testUpdateBeerBadVersion() throws Exception {
        // Do 2 updates and 2 saves, on top of exact same version of an object... second one should throw exceptio in general..
        //   When service-save method is working in 'detached' mode ( just takes the object and tries to save ), second call would fail
        //   When service-save method is doing get/ it will known it is already in a transaction and wait until second one to comit ..
        Beer beer = beerRepository.findAll().get(0);

        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);

        beerDTO.setBeerName("Updated Name");

        MvcResult result = mockMvc.perform(put(BeerController.BASE_PATH_ID, beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());

        beerDTO.setBeerName("Updated Name 2");

        MvcResult result2 = mockMvc.perform(put(BeerController.BASE_PATH_ID, beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent())
                .andReturn();

        System.out.println(result2.getResponse().getStatus());
    }
}