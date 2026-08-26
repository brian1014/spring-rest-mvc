package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.config.SpringSecurityConfig;
import guru.springframework.spring7restmvc.model.CustomerDTO;
import guru.springframework.spring7restmvc.service.CustomerService;
import guru.springframework.spring7restmvc.service.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@ExtendWith(MockitoExtension.class)
@Import(SpringSecurityConfig.class)
class CustomerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomerService customerService;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<CustomerDTO> customerArgumentCaptor;

    CustomerServiceImpl customerServiceImpl;

    @BeforeEach
    void setUp() {
        customerServiceImpl = new CustomerServiceImpl();
    }

    @Test
    void testPatchCustomer() throws Exception {
        CustomerDTO customer = this.customerServiceImpl.getAllCustomers().getFirst();

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("name", "New name");

        mockMvc.perform(patch(CustomerController.CUSTOMER_PATH_ID, customer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNoContent());

        verify(this.customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        assertThat(customer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(customerMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());
    }

    @Test
    void testDeleteCustomer() throws Exception {
        CustomerDTO customer = this.customerServiceImpl.getAllCustomers().getFirst();

        given(customerService.deleteCustomerById(customer.getId())).willReturn(true);

        mockMvc.perform(delete(CustomerController.CUSTOMER_PATH_ID, customer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor))
                .andExpect(status().isNoContent());

        verify(this.customerService).deleteCustomerById(this.uuidArgumentCaptor.capture());

        assertThat(customer.getId()).isEqualTo(this.uuidArgumentCaptor.getValue());
    }

    @Test
    void testUpdateCustomer() throws Exception {
        CustomerDTO customer = this.customerServiceImpl.getAllCustomers().getFirst();

        given(customerService.updateCustomerById(any(UUID.class),any(CustomerDTO.class))).willReturn(Optional.of(customer));

        mockMvc.perform(put(CustomerController.CUSTOMER_PATH_ID, customer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNoContent());

        verify(this.customerService).updateCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        CustomerDTO customerCaptured = customerArgumentCaptor.getValue();
        assertThat(customer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(customer.getName()).isEqualTo(customerCaptured.getName());
    }

    @Test
    void testCreateNewCustomer() throws Exception {
        CustomerDTO customer = customerServiceImpl.getAllCustomers().getFirst();
        customer.setId(null);
        customer.setVersion(null);

        given(customerService.saveNewCustomer(any(CustomerDTO.class))).willReturn(this.customerServiceImpl.getAllCustomers().get(1));

        mockMvc.perform(post(CustomerController.CUSTOMER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        List<CustomerDTO> customers = this.customerServiceImpl.getAllCustomers();
        given(customerService.getAllCustomers()).willReturn(customers);

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH)
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(customers.size())));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {
        given(this.customerService.getCustomerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH_ID, UUID.randomUUID())
                        .with(BeerControllerTest.jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCustomerById() throws Exception {
        CustomerDTO customer = this.customerServiceImpl.getAllCustomers().getFirst();

        given(this.customerService.getCustomerById(customer.getId())).willReturn(Optional.of(customer));

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH_ID, customer.getId())
                        .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(customer.getId().toString())))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }
}