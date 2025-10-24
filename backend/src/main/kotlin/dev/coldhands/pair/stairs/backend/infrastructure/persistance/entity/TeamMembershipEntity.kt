package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
) {
    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: Instant? = null

    @Column(name = "updated_at")
    @UpdateTimestamp
    var updatedAt: Instant? = null
}