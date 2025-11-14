package dev.coldhands.pair.stairs.backend

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc


@ActiveProfiles("inmemory-test")
open class InMemoryAcceptanceTest @Autowired constructor(
    objectMapper: ObjectMapper,
    mockMvc: MockMvc,
    @Value($$"${spring.datasource.url}")
    private val dbUrl: String
) : AbstractAcceptanceTest(objectMapper, mockMvc, dbUrl) {

    public override fun getExpectedDbUrlPrefix(): String = "jdbc:h2"
}