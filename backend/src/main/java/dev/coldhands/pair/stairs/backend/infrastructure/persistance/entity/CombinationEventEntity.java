package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class CombinationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "combination_id")
    private CombinationEntity combination;

    protected CombinationEventEntity() {
    }

    public CombinationEventEntity(LocalDate date, CombinationEntity combination) {
        this.date = date;
        this.combination = combination;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public CombinationEntity getCombination() {
        return combination;
    }

    public void setCombination(CombinationEntity combination) {
        this.combination = combination;
    }
}
