package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*

@Entity
@Table(name = "developers")
class DeveloperEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name")
    val name: String,

    @Column(name = "archived")
    var archived: Boolean
) {
    // todo remove me once everything is kotlin
    constructor(name: String) : this(id = null, name = name, archived = false)
}