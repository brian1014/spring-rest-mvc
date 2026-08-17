package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.service.BeerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerController {
    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final BeerService beerService;

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity<Void> updateBeerPatchById(@PathVariable("beerId")UUID beerId, @RequestBody BeerDTO beerDTO){

        beerService.patchBeerById(beerId, beerDTO);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity<Void> deleteById(@PathVariable("beerId") UUID beerId){

        if (!beerService.deleteById(beerId)) throw new NotFoundException();

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity<Void> updateById(@PathVariable UUID beerId, @RequestBody BeerDTO beerDTO) {
        this.beerService.updateBeerById(beerId, beerDTO).orElseThrow(NotFoundException::new);
//        if (this.beerService.updateBeerById(beerId, beerDTO).isEmpty()) throw new NotFoundException();

        return ResponseEntity.noContent().build();
    }

    @PostMapping(BEER_PATH)
    public ResponseEntity<Void> handlePost(@Validated @RequestBody BeerDTO beerDTO/*, HttpServletRequest request*/) {
        BeerDTO savedBeer = this.beerService.saveNewBeer(beerDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Hola", "Hola");

//        URI location = URI.create(request.getRequestURL().toString() + "/" + savedBeer.getId().toString());
        URI location = UriComponentsBuilder.fromUriString(BEER_PATH_ID)
                .buildAndExpand(savedBeer.getId().toString())
                .encode()
                .toUri();

        return ResponseEntity
                .created(location)
                .headers(headers)
                .build();
    }

    @GetMapping(BEER_PATH)
    public List<BeerDTO> listBeers() {
        return this.beerService.listBeers();
    }

    @GetMapping(BEER_PATH_ID)
    public BeerDTO getBeerById(@PathVariable() UUID beerId) {
        log.debug("Get Beer by Id - in Controller!!!");
        return this.beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
    }
}
