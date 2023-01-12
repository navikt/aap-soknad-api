package no.nav.aap.api.søknad

import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
//@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DataJpaTest
class DBTest {

    @Autowired
    lateinit var userRepository: MinSideBeskjedRepository

  //  @Test
    fun test() {
        val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:14:5").apply {
            start()
        }
        //userRepository.findByFnrAndDoneIsFalse("03016536325")
        assertThat(postgresqlContainer.isRunning).isTrue
    }
}