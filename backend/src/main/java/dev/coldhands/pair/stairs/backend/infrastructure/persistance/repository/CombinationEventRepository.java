package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CombinationEventRepository extends JpaRepository<CombinationEventEntity, Long> {
}
