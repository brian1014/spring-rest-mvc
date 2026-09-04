package guru.springframework.spring7restmvc.listeners;

import guru.springframework.spring7restmvc.entities.BeerAudit;
import guru.springframework.spring7restmvc.events.*;
import guru.springframework.spring7restmvc.mappers.BeerMapper;
import guru.springframework.spring7restmvc.repositories.BeerAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerCreatedListener {
    private final BeerMapper beerMapper;
    private final BeerAuditRepository beerAuditRepository;

    @Async
    @EventListener
    public void listen(BeerEvent event) {
        BeerAudit beerAudit = beerMapper.beerToBeerAudit(event.getBeer());

        String eventType;

        switch (event) {
            case BeerCreatedEvent _ -> eventType = "BEER_CREATED";
            case BeerPatchedEvent _ -> eventType = "BEER_PATCHED";
            case BeerUpdatedEvent _ -> eventType = "BEER_UPDATED";
            case BeerDeletedEvent _ -> eventType = "BEER_DELETED";
            default -> eventType = "UNKNOWN";
        }

        beerAudit.setAuditEventType(eventType);

        if (event.getAuthentication() != null && event.getAuthentication().getName() != null) {
            beerAudit.setPrincipalName(event.getAuthentication().getName());
        }

        BeerAudit savedBeerAudit = beerAuditRepository.save(beerAudit);
        log.debug("Beer Audit saved: {}", savedBeerAudit.getId());
    }
}
