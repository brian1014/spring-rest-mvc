package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.config.SpringSecurityConfig;
import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.service.BeerService;
import guru.springframework.spring7restmvc.service.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
@ExtendWith(MockitoExtension.class)
@Import(SpringSecurityConfig.class)
class BeerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BeerService beerService;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    BeerServiceImpl beerServiceImpl;

    public static final String USERNAME = "user1";
    public static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessor = jwt().jwt(jwt -> {
        jwt.claims(claims -> {
                    claims.put("scope", "message.read");
                    claims.put("scope", "message.write");
                })
                .subject("oidc-client")
                .notBefore(Instant.now().minusSeconds(5L));
    });

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer = this.beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().getFirst();
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New name");

        mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(this.beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());
    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().getFirst();

        given(beerService.deleteById(beer.getId())).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer = this.beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().getFirst();

        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(beer));

        mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());
        BeerDTO capturedBeer = beerArgumentCaptor.getValue();

        assertThat(capturedBeer.getBeerName()).isEqualTo(beer.getBeerName());
        assertThat(capturedBeer.getBeerStyle()).isEqualTo(beer.getBeerStyle());
        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testCreateBeerNullBeerName() throws Exception {
        BeerDTO beer = BeerDTO.builder().build();

        given(beerService.saveNewBeer(any(BeerDTO.class)))
                .willReturn(beerServiceImpl.listBeers(null, null, false, 1,
                25)
                        .getContent().get(1));

        MvcResult mvcResult = mockMvc.perform(post(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testCreateNewBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().getFirst();
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1,
                25).getContent().get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testListBeers() throws Exception {
        Page<BeerDTO> listBeers = beerServiceImpl.listBeers(null, null, false, 1, 25);
        given(beerService.listBeers(any(), any(), any(), any(), any())).willReturn(listBeers);

        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(listBeers.getContent().toArray().length)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {
        given(this.beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().getFirst();

        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get(BeerController.BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
    }
}