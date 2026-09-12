package guru.springframework.spring7restmvc.mappers;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.entities.BeerOrderShipment;
import guru.springframework.spring7restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring7restmvcapi.model.BeerOrderLineDTO;
import guru.springframework.spring7restmvcapi.model.BeerOrderShipmentDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerOrderMapper {

//    @Mapping(target = "beerOrder", ignore = true)
//    BeerOrderShipment beerOrderShipmentDtoToBeerOrderShipment(BeerOrderShipmentDTO beerOrderShipmentDTO);

    BeerOrderShipmentDTO beerOrderShipmentToBeerOrderShipmentDto(BeerOrderShipment beerOrderShipment);

//    @Mapping(target = "beerOrder", ignore = true)
//    BeerOrderLine beerOrderLineDtoToBeerOrderLine(BeerOrderLineDTO beerOrderLineDTO);

    BeerOrderLineDTO beerOrderLineToBeerOrderLineDto(BeerOrderLine beerOrderLine);

//    BeerOrder beerOrderDtoToBeerOrder(BeerOrderDTO beerOrder);

    BeerOrderDTO beerOrderToBeerOrderDto(BeerOrder beerOrder);
}
