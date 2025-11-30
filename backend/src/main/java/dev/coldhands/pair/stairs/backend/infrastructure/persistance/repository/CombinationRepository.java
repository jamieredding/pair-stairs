package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// todo introduce dao
public interface CombinationRepository extends JpaRepository<CombinationEntity, Long> {

    @Query("SELECT c FROM CombinationEntity c WHERE :count = " +
           "(SELECT COUNT(p) FROM PairStreamEntity p WHERE p IN elements(c.pairs) AND p.id IN :pairStreamIds) " +
           "AND :count = (SELECT COUNT(p) FROM PairStreamEntity p WHERE p IN elements(c.pairs))")
    List<CombinationEntity> findByPairStreams(@Param("pairStreamIds") List<Long> pairStreamIds, @Param("count") long count);
}
