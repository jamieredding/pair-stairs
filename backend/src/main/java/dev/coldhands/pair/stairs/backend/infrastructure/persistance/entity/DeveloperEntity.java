package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity;

import jakarta.persistence.*;

@Entity
@Table(name="developers")
public class DeveloperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "archived")
    private boolean archived;

    public DeveloperEntity(String name) {
        this.name = name;
    }

    public DeveloperEntity(long id, String name,  boolean archived) {
        this.id = id;
        this.name = name;
        this.archived = archived;
    }

    protected DeveloperEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
