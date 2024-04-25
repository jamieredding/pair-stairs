package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/calculate")
    List<ScoredAssignment> calculate(@RequestParam("page") Optional<Integer> page,
                                     @RequestBody CalculateInput calculateInput) {
        int requestedPage = page.orElse(0);

        final List<Developer> developers = calculateInput.developers();
        final List<Stream> streams = calculateInput.streams();

        return assignmentService.calculate(developers, streams, requestedPage);
    }
}
