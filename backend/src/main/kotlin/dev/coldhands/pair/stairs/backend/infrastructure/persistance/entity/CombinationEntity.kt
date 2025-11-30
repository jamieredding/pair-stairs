package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*

@Entity
@Table(name = "combinations")
class CombinationEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "combination_pair_member",
        joinColumns = [JoinColumn(name = "combination_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "pair_stream_id", referencedColumnName = "id")]
    )
    val pairs: MutableList<PairStreamEntity>
) {
    // todo remove me once everything is kotlin
    constructor(pairs: List<PairStreamEntity>) : this(id = null, pairs = pairs.toMutableList())
}