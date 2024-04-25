package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamRepository extends JpaRepository<StreamEntity, Long> {
}
