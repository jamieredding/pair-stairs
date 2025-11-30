package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*

@Entity
@Table(name = "pair_streams")
class PairStreamEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "developer_pair_member",
        joinColumns = [JoinColumn(name = "pair_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "developer_id", referencedColumnName = "id")]
    )
    val developers: MutableList<DeveloperEntity>,

    @ManyToOne
    @JoinColumn(name = "stream_id")
    val stream: StreamEntity,
) {
    // todo remove me once everything is kotlin
    constructor(developers: List<DeveloperEntity>, stream: StreamEntity) : this(
        id = null,
        developers = developers.toMutableList(),
        stream = stream
    )
}