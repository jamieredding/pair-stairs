package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oidc_sub")
    private String oidcSub;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    public UserEntity(String oidcSub, String displayName) {
        this.oidcSub = oidcSub;
        this.displayName = displayName;
    }

    protected UserEntity() {

    }

    public Long getId() {
        return id;
    }

    public String getOidcSub() {
        return oidcSub;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
