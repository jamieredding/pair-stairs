package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
    @CreationTimestamp
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    @UpdateTimestamp
    var updatedAt: Instant? = null
)