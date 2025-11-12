package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "team_memberships")
class TeamMembershipEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "display_name")
    val displayName: String,

    @OneToOne
    val user: UserEntity,

    @Suppress("unused")
    @OneToOne
    val team: TeamEntity,

    @Column(name = "created_at")
    val createdAt: Instant,

    @Column(name = "updated_at")
    val updatedAt: Instant
)