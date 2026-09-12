package guru.springframework.spring7restmvc.service;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvcapi.model.BeerOrderCreateDTO;
import guru.springframework.spring7restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring7restmvcapi.model.BeerOrderUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerOrderService {
    Optional<BeerOrderDTO> getById(UUID beerOrderId);

    Page<BeerOrderDTO> listOrders(Integer pageNumber, Integer pageSize);

    BeerOrder createOrder(BeerOrderCreateDTO beerOrderCreateDTO);

    BeerOrderDTO updateOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO);

    void deleteOrder(UUID beerOrderId);
}
