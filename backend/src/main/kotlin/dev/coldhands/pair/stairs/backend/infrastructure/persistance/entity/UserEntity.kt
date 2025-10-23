package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
    @CreationTimestamp
    val createdAt: Instant? = null,

    @Column(name = "updated_at")
    @UpdateTimestamp
    val updatedAt: Instant? = null
)
