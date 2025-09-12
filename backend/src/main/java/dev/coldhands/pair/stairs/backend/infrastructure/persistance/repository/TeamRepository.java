package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findBySlug(String slug);
}
