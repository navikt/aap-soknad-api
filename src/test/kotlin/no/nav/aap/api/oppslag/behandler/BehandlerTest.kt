package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.felles.MockWebServerExtensions.expect
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerKategori.LEGE
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerType.FASTLEGE
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.web.reactive.function.client.WebClient

class BehandlerTest {

    lateinit var server: MockWebServer
    lateinit var client: BehandlerClient

    val respons = """[
              {
                  "type": "FASTLEGE",
                  "kategori": "LEGE",
                  "behandlerRef": "d182f24b-ebca-4f44-bf86-65901ec6141b",
                  "fnr": "08089403198",
                  "fornavn": "Unni",
                  "etternavn": "Larsen",
                  "orgnummer": "976673867",
                  "kontor": "Legesenteret AS",
                  "adresse": "Legeveien 17",
                  "postnummer": "5300",
                  "poststed": "KLEPPESTØ",
                  "telefon": "500000230"
               }
             ]
            """

    @BeforeEach
    fun beforeEach() {
        server = MockWebServer()
        with(BehandlerConfig(server.url("/").toUri())) {
            client = BehandlerClient(BehandlerWebClientAdapter(WebClient.builder().baseUrl("$baseUri").build(),WebClient.create(),this))
        }
    }

    @Test
    @DisplayName("Junk fra tjenesten skal gi en tom list")
    fun junkRespons() {
        server.expect("junk")
        assertThat(client.behandlerInfo()).isEmpty()
    }

    @Test
    @DisplayName("Retry gir opp tilslutt og gir tom liste")
    fun feilRespons() {
        server.expect(4,BAD_GATEWAY)
        assertThat(client.behandlerInfo()).isEmpty()
    }

    @Test
    @DisplayName("Not found gir opp med en gang")
    fun notFound() {
        server.expect(NOT_FOUND)
        assertThat(client.behandlerInfo()).isEmpty()
    }
    @Test
    @DisplayName("Tom respons fra tjenesten skal gi en tom list")
    fun tomRespons() {
        server.expect(OK)
        assertThat(client.behandlerInfo()).isEmpty()
    }

    @Test
    @DisplayName("Normal respons fra tjenesten leses og mappes korrekt")
    fun okRespons() {
        server.expect(respons)
        assertOK()
    }

    @Test
    @DisplayName("Transiente feil skal føre til retry og korrekt respons til slutt")
    fun okResponsEtter2Retries() {
        server
            .expect(2,INTERNAL_SERVER_ERROR)
            .expect(respons)
        assertOK()
    }
    private fun assertOK() {
        with(client.behandlerInfo().single()) {
            assertThat(this).isNotNull
            assertThat(type).isEqualTo(FASTLEGE)
            assertThat(kategori).isEqualTo(LEGE)
            assertThat(navn.fornavn).isEqualTo("Unni")
        }
    }
}