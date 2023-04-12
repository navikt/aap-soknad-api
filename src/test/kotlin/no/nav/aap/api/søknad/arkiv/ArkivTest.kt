package no.nav.aap.api.søknad.arkiv

import java.util.*
import java.util.UUID.*
import kotlin.test.assertEquals
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.reactive.function.client.WebClient.builder
import org.springframework.web.reactive.function.client.WebClient.create
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.MockWebServerExtensions.expect
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.søknad.arkiv.ArkivClient.ArkivResultat
import no.nav.aap.api.søknad.arkiv.Journalpost.AvsenderMottaker
import no.nav.aap.api.søknad.arkiv.Journalpost.Bruker
import no.nav.aap.api.søknad.arkiv.Journalpost.Dokument
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant

class ArkivTest {

    lateinit var arkiv: MockWebServer
    lateinit var client: ArkivClient


     private val kvittering =
         """
         {
           "journalpostId" : "42",
           "journalpostferdigstilt" : false,
           "dokumenter" : [ {
             "dokumentInfoId" : "666"
           } ]
         }
         """


    @BeforeEach
    fun beforeEach() {
        arkiv = MockWebServer()
        val cfg = ArkivConfig(arkiv.url("/").toUri())
        client = ArkivClient(ArkivWebClientAdapter(builder().baseUrl("${cfg.baseUri}").build(), create(), cfg))
    }

    @Test
    @DisplayName("OK respons, happy path")
    fun ok() {
        arkiv.expect(kvittering,CREATED)
        assertOK(client.arkiver(journalpost()))
    }
        @Test
        @DisplayName("Exception kastes etter alle retry-forsøkene er brukt opp")
        fun bad() {
            arkiv.expect(4,BAD_GATEWAY)
            assertThrows<IrrecoverableIntegrationException> {
                client.arkiver(journalpost())
            }
            assertEquals(arkiv.requestCount,4)
    }
    @Test
    @DisplayName("Først respons som fører til retry, deretter 409 OK")
    fun badConflict() {
        arkiv.expect(BAD_GATEWAY).expect(kvittering,CONFLICT)
        assertOK(client.arkiver(journalpost()))
    }

    private fun assertOK(resultat: ArkivResultat) {
        with(resultat)  {
            assertThat(this).isNotNull
            assertThat(journalpostId).isEqualTo("42")
            assertThat(dokumentIds.single()).isEqualTo("666")
        }
    }
    private fun journalpost() = Journalpost("tittel",
            AvsenderMottaker(Fødselsnummer("08089403198"),
            Navn("Test","Tester","Testsen")),
            Bruker(Fødselsnummer("08089403198")), listOf(Dokument("tittel","kode",
            DokumentVariant("fysisk"))), randomUUID())
}