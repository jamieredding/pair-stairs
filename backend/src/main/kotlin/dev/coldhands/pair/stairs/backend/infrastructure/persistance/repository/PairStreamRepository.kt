package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PairStreamRepository: JpaRepository<PairStreamEntity, Long> {
    @Query("""
        SELECT p
        FROM PairStreamEntity p
        JOIN p.developers d
        WHERE p.stream.id = :streamId
          AND d.id IN :developerIds
        GROUP BY p
        HAVING COUNT(DISTINCT d.id) = :count
           AND (
               SELECT COUNT(DISTINCT d2.id)
               FROM PairStreamEntity p2
               JOIN p2.developers d2
               WHERE p2 = p
           ) = :count
        ORDER BY p.id ASC
    """)
    fun findByDevelopersAndStream(
        @Param("developerIds") developerIds: Set<Long>,
        @Param("streamId") streamId: Long,
        @Param("count") count: Long,
    ): List<PairStreamEntity>
}