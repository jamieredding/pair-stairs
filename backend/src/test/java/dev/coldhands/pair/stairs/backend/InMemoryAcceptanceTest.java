package dev.coldhands.pair.stairs.backend;


import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("inmemory-test")
public class InMemoryAcceptanceTest extends AbstractAcceptanceTest {

    @Override
    String getExpectedDbUrlPrefix() {
        return "jdbc:h2";
    }
}
