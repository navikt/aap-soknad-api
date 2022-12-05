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
import org.springframework.web.reactive.function.client.WebClient

class ArkivTest {

    lateinit var joark: MockWebServer
    lateinit var client: ArkivClient


     private val respons =
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
        joark = MockWebServer()
        val cfg = ArkivConfig(joark.url("/").toUri())
        client = ArkivClient(ArkivWebClientAdapter(WebClient.builder().baseUrl("${cfg.baseUri}").build(), WebClient.create(), cfg))
    }

    @Test
    fun ok() {
        joark.expect(respons)
        var r = client.arkiver(journalpost())
        assertNotNull(r)
        assertThat(r.journalpostId).isEqualTo("42")

    }

    private fun journalpost() = Journalpost("tittel",
            AvsenderMottaker(Fødselsnummer("03016536325"),
            Navn("Jan","Olav","Eide")),
            Bruker(Fødselsnummer("03016536325")), listOf(Dokument("tittel","kode",
            DokumentVariant("fysisk"))), UUID.randomUUID())

}