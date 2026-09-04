package guru.springframework.spring7restmvc.repositories;

import guru.springframework.spring7restmvc.entities.BeerAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BeerAuditRepository extends JpaRepository<BeerAudit, UUID> {
}
