package dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "pair_streams")
public class PairStreamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "developer_pair_member",
            joinColumns = @JoinColumn(name = "pair_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "developer_id", referencedColumnName = "id")
    )
    private List<DeveloperEntity> developers;

    @ManyToOne
    @JoinColumn(name = "stream_id")
    private StreamEntity stream;

    protected PairStreamEntity() {
    }

    public PairStreamEntity(List<DeveloperEntity> developers, StreamEntity stream) {
        this.developers = developers;
        this.stream = stream;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<DeveloperEntity> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<DeveloperEntity> developers) {
        this.developers = developers;
    }

    public StreamEntity getStream() {
        return stream;
    }

    public void setStream(StreamEntity stream) {
        this.stream = stream;
    }
}
