package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface CombinationEventRepository : JpaRepository<CombinationEventEntity, Long> {

    // todo delete me in favour of Dao method
    fun getMostRecentCombinationEvents(count: Int): List<CombinationEventEntity> {
        val pageRequest = PageRequest.of(0, count, Sort.by(DESC, "date"))
        return findAll(pageRequest).toList()
    }

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p JOIN p.developers d WHERE d.id = :developerId")
    fun findByDeveloperId(@Param("developerId") developerId: Long): List<CombinationEventEntity>

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p JOIN p.developers d WHERE d.id = :developerId AND c.date BETWEEN :startDate AND :endDate")
    fun findByDeveloperIdBetween(
        @Param("developerId") developerId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<CombinationEventEntity>

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p WHERE p.stream.id = :streamId")
    fun findByStreamId(@Param("streamId") streamId: Long): List<CombinationEventEntity>

    @Query("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p WHERE p.stream.id = :streamId AND c.date BETWEEN :startDate AND :endDate")
    fun findByStreamIdBetween(
        @Param("streamId") streamId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<CombinationEventEntity>
}