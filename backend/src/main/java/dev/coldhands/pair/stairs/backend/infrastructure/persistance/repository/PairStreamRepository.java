package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// todo introduce dao
public interface PairStreamRepository extends JpaRepository<PairStreamEntity, Long> {

    @Query("SELECT p FROM PairStreamEntity p JOIN p.developers d WHERE p.stream = :stream AND d.id IN :developerIds GROUP BY p HAVING COUNT(d) = :count AND (SELECT COUNT(d2) FROM PairStreamEntity p2 JOIN p2.developers d2 WHERE p2 = p) = :count")
    List<PairStreamEntity> findByDevelopersAndStream(@Param("developerIds") List<Long> developerIds, @Param("stream") StreamEntity stream, @Param("count") long count);
}
