package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "oidc_sub")
    val oidcSub: String,

    @Column(name = "display_name")
    var displayName: String,

    @Column(name = "created_at")
    val createdAt: Instant,

    @Column(name = "updated_at")
    var updatedAt: Instant
)
