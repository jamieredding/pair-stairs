package dev.coldhands.pair.stairs.backend;

import dev.coldhands.pair.stairs.backend.domain.Developer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperRepository extends JpaRepository<Developer, Long> {
}
