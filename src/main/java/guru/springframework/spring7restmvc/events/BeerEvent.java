package guru.springframework.spring7restmvc.events;

import guru.springframework.spring7restmvc.entities.Beer;
import org.springframework.security.core.Authentication;

public interface BeerEvent {
    Beer getBeer();

    Authentication getAuthentication();
}
