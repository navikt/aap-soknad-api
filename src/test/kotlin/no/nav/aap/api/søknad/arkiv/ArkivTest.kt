package no.nav.aap.api.søknad.arkiv

import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.MockWebServerExtensions.expect
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.søknad.arkiv.Journalpost.AvsenderMottaker
import no.nav.aap.api.søknad.arkiv.Journalpost.Bruker
import no.nav.aap.api.søknad.arkiv.Journalpost.Dokument
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.CONFLICT
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
        val arkivAdapter = ArkivWebClientAdapter(WebClient.builder().baseUrl("${cfg.baseUri}").build(),WebClient.create(),cfg)
        client = ArkivClient(arkivAdapter)
    }

    @Test
    fun conflictErOK() {
        joark.expect(respons,CONFLICT)
       // var r = client.arkiver(journalpost())
       // println(r)

    }

    private fun journalpost() = Journalpost("tittel",
            AvsenderMottaker(Fødselsnummer("03016536325"),
            Navn("Jan","Olav","Eide")),
            Bruker(Fødselsnummer("03016536325")), listOf(Dokument("tittel","kode",
            DokumentVariant("fysisk"))), UUID.randomUUID())

}