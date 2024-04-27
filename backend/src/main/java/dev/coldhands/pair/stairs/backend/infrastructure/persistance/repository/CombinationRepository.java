package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CombinationRepository extends JpaRepository<CombinationEntity, Long> {

    @Query("SELECT c FROM CombinationEntity c JOIN c.pairs p WHERE p.id IN :pairStreamIds GROUP BY p HAVING COUNT(p) = :count")
    List<CombinationEntity> findByPairStreams(@Param("pairStreamIds") List<Long> pairStreamIds, @Param("count") long count);
}
