package no.nav.aap.api.søknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class DBTest {

    @Test
    fun test() {
        val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:14").apply {
            start()
            print(username + " " + jdbcUrl + " " + password)

        }
        assertThat(postgresqlContainer.isRunning).isTrue;
    }
}