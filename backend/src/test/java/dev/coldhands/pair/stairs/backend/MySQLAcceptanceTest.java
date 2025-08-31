package dev.coldhands.pair.stairs.backend;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("mysql-test")
public class MySQLAcceptanceTest extends AbstractAcceptanceTest {

    @Override
    String getExpectedDbUrlPrefix() {
        return "jdbc:mysql";
    }
}
