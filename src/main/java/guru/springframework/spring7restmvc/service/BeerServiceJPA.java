package guru.springframework.spring7restmvc.service;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.events.BeerCreatedEvent;
import guru.springframework.spring7restmvc.events.BeerDeletedEvent;
import guru.springframework.spring7restmvc.events.BeerPatchedEvent;
import guru.springframework.spring7restmvc.events.BeerUpdatedEvent;
import guru.springframework.spring7restmvc.mappers.BeerMapper;
import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.model.BeerStyle;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Cacheable(cacheNames = "beerListCache")
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                   Integer pageNumber, Integer pageSize) {
        log.debug("List Beers - in service");
        PageRequest pageRequest = this.buildPageRequest(pageNumber, pageSize);
        Page<Beer> beerPage;

        if (StringUtils.hasText(beerName) && beerStyle == null) {
            beerPage = this.listBeersByName(beerName, pageRequest);
        } else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = this.listBeersByStyle(beerStyle, pageRequest);
        } else if(StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory) {
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }

        return beerPage.map(beerMapper::beerToBeerDTO);
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        if (pageNumber > 0) {
            pageNumber = pageNumber - 1;
        }

        if (pageSize > 1000) {
            pageSize = 1000;
        }

        Sort sort = Sort.by(Sort.Order.asc("beerName"));

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, Pageable pageable) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageable);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, Pageable pageable) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageable);
    }

    private Page<Beer> listBeersByName(String beerName, Pageable pageable) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageable);
    }

    @Override
    @Cacheable(cacheNames = "beerCache", key = "#id")
    public Optional<BeerDTO> getBeerById(UUID id) {
        log.info("Get Beer by Id - in service");
        return Optional.ofNullable(beerMapper.beerToBeerDTO(beerRepository.findById(id).orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beerDTO) {
        clearBeerListCache();

        Beer savedBeer = beerRepository.save(beerMapper.beerDtoToBeer(beerDTO));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        applicationEventPublisher.publishEvent(new BeerCreatedEvent(savedBeer, auth));

        return beerMapper.beerToBeerDTO(savedBeer);
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beerDTO) {
        clearCache(beerId);
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            foundBeer.setBeerName(beerDTO.getBeerName());
            foundBeer.setBeerStyle(beerDTO.getBeerStyle());
            foundBeer.setUpc(beerDTO.getUpc());
            foundBeer.setPrice(beerDTO.getPrice());
            foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());

            var savedBeer = beerRepository.save(foundBeer);

            var auth = SecurityContextHolder.getContext().getAuthentication();

            applicationEventPublisher.publishEvent(new BeerUpdatedEvent(savedBeer, auth));

            atomicReference.set(Optional.of(beerMapper.beerToBeerDTO(savedBeer)));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }
//    ESTO NO FUNCIONA PORQUE ESTAN EN LA MISMA CLASE
//    @Caching(evict = {
//            @CacheEvict(cacheNames = "beerCache", key = "#beerId"),
//            @CacheEvict(cacheNames = "beerListCache"),
//    })
    @Override
    public Boolean deleteById(UUID beerId) {
        clearCache(beerId);
        if (!beerRepository.existsById(beerId)) return false;

        val auth = SecurityContextHolder.getContext().getAuthentication();

        applicationEventPublisher.publishEvent(new BeerDeletedEvent(Beer.builder().id(beerId).build(), auth));

        beerRepository.deleteById(beerId);
        return true;
    }


    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        clearCache(beerId);
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if (StringUtils.hasText(beer.getBeerName())){
                foundBeer.setBeerName(beer.getBeerName());
            }
            if (beer.getBeerStyle() != null){
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if (StringUtils.hasText(beer.getUpc())){
                foundBeer.setUpc(beer.getUpc());
            }
            if (beer.getPrice() != null){
                foundBeer.setPrice(beer.getPrice());
            }
            if (beer.getQuantityOnHand() != null){
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }
            var savedBeer = beerRepository.save(foundBeer);
            var auth = SecurityContextHolder.getContext().getAuthentication();

            applicationEventPublisher.publishEvent(new BeerPatchedEvent(savedBeer, auth));

            atomicReference.set(Optional.of(beerMapper
                    .beerToBeerDTO(savedBeer)));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    private void clearCache(UUID beerId) {
        Cache beerCache = cacheManager.getCache("beerCache");

        if (beerCache != null) {
            beerCache.evict(beerId);
        }
        clearBeerListCache();
    }

    private void clearBeerListCache() {
        Cache beerListCache = cacheManager.getCache("beerListCache");
        if (beerListCache != null) {
            beerListCache.clear();
        }
    }
}
