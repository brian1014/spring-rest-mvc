package guru.springframework.spring7restmvc.mappers;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {
    Beer beerDtoToBeer(BeerDTO dto);
    BeerDTO beerToBeerDTO(Beer beer);
}
