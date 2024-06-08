package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CombinationEventRepository extends JpaRepository<CombinationEventEntity, Long> {

    default List<CombinationEventEntity> getMostRecentCombinationEvents(int count) {
        final PageRequest pageRequest = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "date"));
        return findAll(pageRequest).toList();
    }

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p JOIN p.developers d WHERE d.id = :developerId")
    List<CombinationEventEntity> findByDeveloperId(@Param("developerId") long developerId);

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p JOIN p.developers d WHERE d.id = :developerId AND c.date BETWEEN :startDate AND :endDate")
    List<CombinationEventEntity> findByDeveloperIdBetween(@Param("developerId") long developerId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
}
