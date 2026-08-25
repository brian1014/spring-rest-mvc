package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.entities.Customer;
import guru.springframework.spring7restmvc.mappers.CustomerMapper;
import guru.springframework.spring7restmvc.model.CustomerDTO;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerControllerIT {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerMapper customerMapper;

    @Test
    void testDeleteByIdNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.deleteCustomerById(UUID.randomUUID()));
    }

    @Test
    void testDeleteById() {
        Customer customer = customerRepository.findAll().getFirst();

        ResponseEntity<Void> responseEntity = customerController.deleteCustomerById(customer.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.updateCustomerByID(UUID.randomUUID(), CustomerDTO.builder().build());
        });
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateExistingCustomer() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);

        customerDTO.setId(null);
        customerDTO.setVersion(null);

        final String customerName = "UPDATED";
        customerDTO.setName(customerName);

        ResponseEntity<Void> responseEntity = customerController.updateCustomerByID(customer.getId(), customerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElse(null);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getName()).isEqualTo(customerName);
    }

    @Test
    @Rollback
    @Transactional
    void testSaveNewCustomer() {
        CustomerDTO customerDTO = CustomerDTO.builder()
                .name("New Customer")
        .build();

        ResponseEntity<Void> responseEntity = customerController.handlePost(customerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[locationUUID.length - 1]);

        Customer customer = customerRepository.findById(savedUUID).orElse(null);

        assertThat(customer).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void testListAllEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos.size()).isEqualTo(0);
    }

    @Test
    void testListAll() {
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos.size()).isEqualTo(3);
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
           customerController.getCustomerById(UUID.randomUUID());
        });
    }

    @Test
    void testGetById() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());

        assertThat(customerDTO).isNotNull();
    }
}