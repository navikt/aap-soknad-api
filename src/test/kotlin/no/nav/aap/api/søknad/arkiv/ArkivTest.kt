package no.nav.aap.api.søknad.arkiv

import java.util.*
import kotlin.test.assertNotNull
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.MockWebServerExtensions.expect
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.søknad.arkiv.Journalpost.AvsenderMottaker
import no.nav.aap.api.søknad.arkiv.Journalpost.Bruker
import no.nav.aap.api.søknad.arkiv.Journalpost.Dokument
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.reactive.function.client.WebClient.builder
import org.springframework.web.reactive.function.client.WebClient.create

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
    fun ok() {
        arkiv.expect(kvittering,CREATED)
        var r = client.arkiver(journalpost())
        assertNotNull(r)
        assertThat(r.journalpostId).isEqualTo("42")
    }
   // @Test
    fun conflict() {
        arkiv.expect(kvittering,CONFLICT)
        var r = client.arkiver(journalpost())
        assertNotNull(r)
        assertThat(r.journalpostId).isEqualTo("42")

    }

    private fun journalpost() = Journalpost("tittel",
            AvsenderMottaker(Fødselsnummer("08089403198"),
            Navn("Test","Tester","Testsen")),
            Bruker(Fødselsnummer("08089403198")), listOf(Dokument("tittel","kode",
            DokumentVariant("fysisk"))), UUID.randomUUID())

}