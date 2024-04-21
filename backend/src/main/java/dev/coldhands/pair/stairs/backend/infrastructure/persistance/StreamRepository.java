package dev.coldhands.pair.stairs.backend.infrastructure.persistance;

import dev.coldhands.pair.stairs.backend.domain.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamRepository extends JpaRepository<Stream, Long> {
}
