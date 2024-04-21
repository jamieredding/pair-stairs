package dev.coldhands.pair.stairs.backend.domain;

import java.util.List;

public interface AssignmentService {

    List<ScoredAssignment> calculate(List<Developer> developers, List<Stream> streams, int page);
}
