package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "teams")
class TeamEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    val slug: String,

    @Column(name = "created_at")
    val createdAt: Instant,

    @Column(name = "updated_at")
    var updatedAt: Instant
)