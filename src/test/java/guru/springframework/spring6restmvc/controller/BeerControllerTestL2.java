package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.service.BeerService;
import guru.springframework.spring6restmvc.service.IBeerService;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

//import static net.bytebuddy.matcher.ElementMatchers.is;  // IntelliJ misuses the byteBuddy instead of org.hamcrest.core.Is.is
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest - Level1
// WebMvcTest - limit only to our controller
// No need to autowire our controller, as we'll use the Mock MVC Context
@WebMvcTest(BeerController.class)
class BeerControllerTestL2 {

//    @Autowired
//    BeerController beerController;
    @Autowired
    MockMvc mockMvc;

    @MockBean
    IBeerService beerService;

    @Autowired
    ObjectMapper objectMapper;

    BeerService beerSvcImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgCaptor;

    @Captor
    ArgumentCaptor<Beer> beerArgCaptor;

    @BeforeEach
    void setUp() {
        beerSvcImpl = new BeerService();
    }

    @Test
    void getBeerById() throws Exception {
        // acts as dummy call to also initialize the internal map with content
        Beer testBeer = beerSvcImpl.listBeers().get(0);

        //given(beerService.getBeerById(any(UUID.class))).willReturn(testBear);
        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get("/api/v1/beer/" + testBeer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
        ;
    }

    @Test
    void testListBeer() throws Exception {
        given(beerService.listBeers()).willReturn(beerSvcImpl.listBeers());

        mockMvc.perform(get("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void testCreateNewBeer() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Beer beer = beerSvcImpl.listBeers().get(0);

        System.out.println(objectMapper.writeValueAsString(beer));
    }

    @Test
    void testCreateNewBeer2() throws Exception {
        Beer beer = beerSvcImpl.listBeers().get(0);
        //System.out.println(objectMapper.writeValueAsString(beer));
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(Beer.class))).willReturn(beerSvcImpl.listBeers().get(1));

        mockMvc.perform(post("/api/v1/beer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateBeer() throws Exception {
        Beer beer = beerSvcImpl.listBeers().get(0);

        mockMvc.perform(put("/api/v1/beer/" + beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());  // HTTP 204

        // alternative to any UUID --> argument capture
        verify(beerService).updateBeerById(any(UUID.class), any(Beer.class));
    }

    @Test
    void testDeleteBeer() throws Exception {
        Beer beer = beerSvcImpl.listBeers().get(0);

        // try DRY refactoring
        //mockMvc.perform(delete(BeerController.BASE_PATH, "/", beer.getId())
        mockMvc.perform(delete("/api/v1/beer/" + beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        //verify(beerService).deleteById(any(UUID.class));

        // ARGUMENT CAPTOR - locally or get it injected..
        //ArgumentCaptor<UUID> uuidArgCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(beerService).deleteById(uuidArgCaptor.capture());
        assertThat(beer.getId()).isEqualTo(uuidArgCaptor.getValue());
    }

    @Test
    void testPatchBeer() throws Exception {
        Beer beer = beerSvcImpl.listBeers().get(0);

        // set some adhoc JSON for testing
        // not sending fully qualified object, but only the modified properties
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");

        mockMvc.perform(patch("/api/v1/beer/" + beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchById(uuidArgCaptor.capture(), beerArgCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgCaptor.getValue().getBeerName());

    }

    @Test
    void getBeerByIdNotFound() throws Exception {
        //given(beerService.getBeerById(any(UUID.class))).willThrow(NotFoundException.class);  // in case we did not use Optional
        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BASE_PATH + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}