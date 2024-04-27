package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class CombinationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "combination_pair_member",
            joinColumns = @JoinColumn(name = "combination_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "pair_stream_id", referencedColumnName = "id")
    )
    private List<PairStreamEntity> pairs;

    protected CombinationEntity() {
    }

    public CombinationEntity(List<PairStreamEntity> pairs) {
        this.pairs = pairs;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<PairStreamEntity> getPairs() {
        return pairs;
    }

    public void setPairs(List<PairStreamEntity> pairs) {
        this.pairs = pairs;
    }
}
