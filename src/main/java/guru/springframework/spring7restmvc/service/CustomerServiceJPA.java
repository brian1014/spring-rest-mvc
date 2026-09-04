package guru.springframework.spring7restmvc.service;

import guru.springframework.spring7restmvc.mappers.CustomerMapper;
import guru.springframework.spring7restmvc.model.CustomerDTO;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CustomerServiceJPA implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CacheManager cacheManager;

    @Override
    @Cacheable(cacheNames = "customerCache")
    public Optional<CustomerDTO> getCustomerById(UUID uuid) {
        return Optional.ofNullable(customerMapper.customerToCustomerDTO(customerRepository.findById(uuid).orElse(null)));
    }

    @Override
    @Cacheable(cacheNames = "customerListCache")
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::customerToCustomerDTO)
                .toList();
    }

    @Override
    public CustomerDTO saveNewCustomer(CustomerDTO customerDTO) {
        clearListCacheCustomer();
        return customerMapper.customerToCustomerDTO(customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO)));
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customerDTO) {
        clearCache(customerId);

        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();
        customerRepository.findById(customerId).ifPresentOrElse((foundCustomer) -> {
            foundCustomer.setName(customerDTO.getName());
            atomicReference.set(Optional.of(customerMapper.customerToCustomerDTO(customerRepository.save(foundCustomer))));
        }, () ->
            atomicReference.set(Optional.empty())
        );
        return atomicReference.get();
    }

    @Override
    public boolean deleteCustomerById(UUID customerId) {
        clearCache(customerId);
        if (!customerRepository.existsById(customerId)) return false;

        customerRepository.deleteById(customerId);
        return true;
    }

    @Override
    public Optional<CustomerDTO> patchCustomerById(UUID customerId, CustomerDTO customerDTO) {
        clearCache(customerId);
        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();

        customerRepository.findById(customerId).ifPresentOrElse(foundCustomer -> {
            if (StringUtils.hasText(customerDTO.getName())){
                foundCustomer.setName(customerDTO.getName());
            }
            atomicReference.set(Optional.of(customerMapper
                    .customerToCustomerDTO(customerRepository.save(foundCustomer))));
        }, () ->
            atomicReference.set(Optional.empty())
        );

        return atomicReference.get();
    }

    private void clearCache(UUID customerId) {
        Cache customerCache = cacheManager.getCache("customerCache");
        if (customerCache != null) {
            customerCache.evict(customerId);
        }

        clearListCacheCustomer();
    }
    private void clearListCacheCustomer() {
        Cache customerListCache = cacheManager.getCache("customerListCache");

        if (customerListCache != null) {
            customerListCache.clear();
        }
    }
}
