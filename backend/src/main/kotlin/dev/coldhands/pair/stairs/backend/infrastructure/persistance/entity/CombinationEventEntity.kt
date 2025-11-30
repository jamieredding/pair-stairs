package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "combination_events")
class CombinationEventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "combination_id")
    val combination: CombinationEntity
) {
    // todo remove me once everything is kotlin
    constructor(date: LocalDate, combination: CombinationEntity) : this(id = null, date = date, combination = combination)
}