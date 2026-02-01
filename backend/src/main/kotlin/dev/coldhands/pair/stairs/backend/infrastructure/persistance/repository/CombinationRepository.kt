package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CombinationRepository : JpaRepository<CombinationEntity, Long> {

    @Query(
        """
        SELECT c
        FROM CombinationEntity c
        JOIN c.pairs p
        WHERE p.id IN :pairStreamIds
        GROUP BY c
        HAVING COUNT(DISTINCT p.id) = :count
           AND (
               SELECT COUNT(p2)
               FROM CombinationEntity c2
               JOIN c2.pairs p2
               WHERE c2 = c
           ) = :count
        ORDER BY c.id ASC
    """)
    fun findByPairStreams(
        @Param("pairStreamIds") pairStreamIds: List<Long>,
        @Param("count") count: Long,
    ): List<CombinationEntity>
}