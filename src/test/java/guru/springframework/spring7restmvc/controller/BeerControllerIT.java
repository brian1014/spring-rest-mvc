package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.events.BeerCreatedEvent;
import guru.springframework.spring7restmvc.events.BeerDeletedEvent;
import guru.springframework.spring7restmvc.events.BeerPatchedEvent;
import guru.springframework.spring7restmvc.events.BeerUpdatedEvent;
import guru.springframework.spring7restmvc.mappers.BeerMapper;
import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.model.BeerStyle;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RecordApplicationEvents
@SpringBootTest
class BeerControllerIT {

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testUpdateBeer() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();

        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setBeerName("Update Name");

        mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent())
                .andReturn();

        Assertions.assertEquals(1, applicationEvents.stream(BeerUpdatedEvent.class).count());
    }

    @Test
    void testCreateBeerMVC() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New Beer")
                .beerStyle(BeerStyle.IPA)
                .upc("12321")
                .price(BigDecimal.TEN)
                .quantityOnHand(5)
                .build();

        mockMvc.perform(post(BeerController.BEER_PATH)
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Assertions.assertEquals(1, applicationEvents.stream(BeerCreatedEvent.class).count());
    }

    @Test
    void testPatchBeerMvc() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");

        mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent())
                .andReturn();

        Assertions.assertEquals(1, applicationEvents
                .stream(BeerPatchedEvent.class)
                .count());
    }

    @Test
    void deleteByIdFoundMVC() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();

        mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        Assertions.assertEquals(1, applicationEvents
                .stream(BeerDeletedEvent.class)
                .count());
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryTruePage2() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA")
                        .queryParam("showInventory", "true")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                    .queryParam("beerName", "IPA")
                    .queryParam("showInventory", "false")
                        .queryParam("pageSize", "350")
                    .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.nullValue()));
    }
    @Test
    void testListBeersByStyleAndNameShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                    .queryParam("beerName", "IPA")
                    .queryParam("showInventory", "true")
                        .queryParam("pageSize", "350")
                    .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByStyleAndName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .queryParam("beerName", "IPA")
                        .queryParam("pageSize", "350")
                .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)));
    }

    @Test
    void testNoAuth() throws Exception {
            mockMvc.perform(get(BeerController.BEER_PATH)
                            .queryParam("pageSize", "550")
                            .queryParam("beerStyle", BeerStyle.IPA.name()))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    void testListBeersByStyle() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .queryParam("pageSize", "550")
                .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(548)));
    }
    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .queryParam("pageSize", "350")
                .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(336)));
    }

    @Test
    void testPatchBeerBadName() throws Exception {
        Beer beer = this.beerRepository.findAll().getFirst();

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New name".repeat(100));

        /*MvcResult result = */mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andReturn();



    }

    @Test
    void testDeleteByIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.deleteById(UUID.randomUUID()));
    }

    @Test
    @Transactional
    @Rollback
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().getFirst();

        ResponseEntity<Void> responseEntity = beerController.deleteById(beer.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(beerRepository.findById(beer.getId())).isEmpty();
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build()));
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateExistingBeer() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);

        beerDTO.setId(null);
        beerDTO.setVersion(null);

        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);

        ResponseEntity<Void> responseEntity = beerController.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Beer updatedBeer = beerRepository.findById(beer.getId()).orElse(null);
        assertThat(updatedBeer).isNotNull();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    @Transactional
    @Rollback
    void testSaveNewBeer() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New Beer")
                .build();

        ResponseEntity<Void> responseEntity = beerController.handlePost(beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");

        UUID savedUUID = UUID.fromString(locationUUID[locationUUID.length - 1]);
        Beer beer = beerRepository.findById(savedUUID).orElse(null);

        assertThat(beer).isNotNull();
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.getBeerById(UUID.randomUUID()));
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

    @Test
    void testListBeers() {
        Page<BeerDTO> dtos = beerController.listBeers(null, null, false, 1, 25);
        assertThat(dtos.getContent().size()).isEqualTo(25);
    }

    @Test
    @Transactional
    @Rollback
    void testEmptyList() {
        beerRepository.deleteAll();
        Page<BeerDTO> dtos = beerController.listBeers(null, null, false, 1, 25);

        assertThat(dtos.getContent().size()).isEqualTo(0);
    }
}