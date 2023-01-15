package no.nav.aap.api.søknad

import java.net.URI
import java.time.Duration
import java.util.*
import kotlin.test.assertEquals
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.VedleggEtterspørsel
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Companion.SISTE_SØKNAD
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.BacklinksConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.NAISConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.TopicConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.UtkastConfig
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository
import no.nav.aap.api.søknad.minside.MinSideProdusenter
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository
import no.nav.aap.api.søknad.model.VedleggType.*
import no.nav.aap.util.AuthContext
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.concurrent.ListenableFuture
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
class DBTest {

    @Autowired
    lateinit var søknadRepo: SøknadRepository

    @Autowired
    lateinit var beskjedRepo: MinSideBeskjedRepository

    @Autowired
    lateinit var oppgaveRepo: MinSideOppgaveRepository

    @Autowired
    lateinit var utkastRepo: MinSideUtkastRepository

    @Mock
    lateinit var  arkivClient: ArkivOppslagClient
    @Mock
    lateinit var ctx: AuthContext

    @Mock
    lateinit var avro: KafkaOperations<NokkelInput, Any>

    @Mock
    lateinit var utkast: KafkaOperations<String, String>

    @Mock
    lateinit var result: ListenableFuture<SendResult<NokkelInput, Any>>



    @Test
    fun testEtterspørrManglende() {
        val minSide = MinSideClient(MinSideProdusenter(avro,utkast),CFG, MinSideRepositories(beskjedRepo,oppgaveRepo,utkastRepo,søknadRepo))
        `when`(ctx.getFnr()).thenReturn(FNR)
        `when`(avro.send(any<ProducerRecord<NokkelInput,Any>>())).thenReturn(result)
        `when`(result.get()).thenReturn(RESULT)

        val søknadClient = SøknadClient(søknadRepo,arkivClient,minSide,ctx)
        val uuid =  UUID.randomUUID()
        søknadRepo.save(Søknad(fnr = FNR.fnr, journalpostid = "42", eventid = uuid))
        søknadClient.etterspørrVedlegg(VedleggEtterspørsel(FNR, ANNET))
        søknadClient.søknader(FNR, SISTE_SØKNAD).first()?.let {
            assertEquals(1,it.manglendeVedlegg.size)
            assertEquals(0,it.innsendteVedlegg.size)
            assertEquals(uuid,it.søknadId)
        }
        assertEquals(1,søknadRepo.getSøknadByFnr(FNR.fnr, SISTE_SØKNAD).first().oppgaver.size)
    }

    companion object  {
        private val RESULT = SendResult<NokkelInput, Any>(null, RecordMetadata(TopicPartition("p",1),0,0,0,0,0))
        private val CFG = MinSideConfig(NAISConfig("ns","app"),
                TopicConfig("beskjed",Duration.ofDays(1), true, emptyList(),4),
                TopicConfig("oppgave",Duration.ofDays(1), true, emptyList(),4),
                UtkastConfig("utkast", true),
                true,
                BacklinksConfig(URI.create("http://www.vg.no"),URI.create("http://www.vg.no"),URI.create("http://www.vg.no")),"done")
        private val  FNR = Fødselsnummer("08089403198")
        @BeforeAll
        internal fun startDB() {
            PostgreSQLContainer<Nothing>("postgres:14:5").apply { start()
            }
        }
    }
}