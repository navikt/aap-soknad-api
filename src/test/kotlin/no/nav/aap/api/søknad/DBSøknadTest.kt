package no.nav.aap.api.søknad

import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import java.net.URI
import java.time.Duration.*
import java.util.*
import java.util.concurrent.CompletableFuture
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.*
import org.junit.jupiter.api.fail
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.VedleggEtterspørsel
import no.nav.aap.api.søknad.SøknadTest.Companion.standardSøknad
import no.nav.aap.api.søknad.arkiv.ArkivClient.ArkivResultat
import no.nav.aap.api.søknad.fordeling.SøknadFullfører
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Companion.SISTE_SØKNAD
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.MellomlagringBucketConfig
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.minside.MinSideBeskjedRepository
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.BacklinksConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.ForsideConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.NAISConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.TopicConfig
import no.nav.aap.api.søknad.minside.MinSideConfig.UtkastConfig
import no.nav.aap.api.søknad.minside.MinSideForside
import no.nav.aap.api.søknad.minside.MinSideOppgaveRepository
import no.nav.aap.api.søknad.minside.MinSideProdusenter
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.api.søknad.minside.MinSideUtkastRepository
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.api.søknad.model.VedleggType.*
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.TEST
import no.nav.brukernotifikasjon.schemas.input.NokkelInput

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles(TEST)
@AutoConfigureTestDatabase(replace = NONE)
@DataJpaTest
@TestInstance(PER_CLASS)
class DBSøknadTest {

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
    lateinit var forside: KafkaOperations<Fødselsnummer, MinSideForside>

    @Mock
    lateinit var result: CompletableFuture<SendResult<NokkelInput, Any>>

    @Mock
    lateinit var forsideResult: CompletableFuture<SendResult<Fødselsnummer, MinSideForside>>


    @BeforeAll
    internal fun startDB() {
        PostgreSQLContainer<Nothing>("postgres:14:5").apply { start()
        }
    }

    @BeforeEach
    fun init() {
        `when`(ctx.getFnr()).thenReturn(FNR)
        `when`(avro.send(any<ProducerRecord<NokkelInput,Any>>())).thenReturn(result)
        `when`(result.get()).thenReturn(RESULT)
        `when`(forside.send(any<ProducerRecord<Fødselsnummer,MinSideForside>>())).thenReturn(forsideResult)
        `when`(forsideResult.get()).thenReturn(FORSIDERESULT)


    }

    @Test
    fun etterspørrVedlegg() {
        søknadRepo.getSøknadByEttersendingJournalpostid("42")
        val minSide = MinSideClient(MinSideProdusenter(avro,utkast,forside),CFG, MinSideRepositories(beskjedRepo,oppgaveRepo,utkastRepo,søknadRepo))
        val fullfører = SøknadFullfører(InMemoryDokumentLager(), minSide, søknadRepo, InMemoryMellomLager(FNR), Metrikker(
                LoggingMeterRegistry()))
        val søknadClient = SøknadClient(søknadRepo,arkivClient,minSide,ctx)
        val søknadId = fullfører.fullfør(FNR, standardSøknad(), ARKIVRESULTAT).uuid ?: fail("Søknad ikke registrert")
        val søknad = søknadRepo.getSøknadByFnr(FNR.fnr,SISTE_SØKNAD).single()
        val oppgaveId = søknadClient.etterspørrVedlegg(VedleggEtterspørsel(FNR,LÅNEKASSEN_LÅN)) ?: fail("Etterspørsel ikke registrert")
        val dto = søknadClient.søknad(søknadId) ?:  fail("Søknad ikke registrert")
        assertThat(dto.manglendeVedlegg).hasSize(2)
        with(søknad) {
            assertThat(innsendtevedlegg).hasSize(2)
            assertThat(oppgaver).hasSize(2)
            assertThat(oppgaver.first().eventid).isEqualTo(søknadId)
            assertThat(oppgaver.last().eventid).isEqualTo(oppgaveId)
            fullfører.fullfør(FNR, ettesending(eventid,LÅNEKASSEN_LÅN), ARKIVRESULTAT)
            assertThat(oppgaver).extracting("eventid").doesNotContain(oppgaveId)
            assertThat(oppgaver).extracting("eventid").contains(søknad.eventid )
            assertThat(oppgaver).hasSize(1)
            fullfører.fullfør(FNR, ettesending(eventid,ANDREBARN), ARKIVRESULTAT)
            assertThat(oppgaver).isEmpty()
            assertThat(søknadClient.søknader(FNR, SISTE_SØKNAD).first().manglendeVedlegg).isEmpty()
            assertThat(søknadClient.søknad(søknadId)?.manglendeVedlegg).isEmpty()
        }
    }

    companion object  {
        private val NAV = URI.create("http://www.nav.no")
        private val ARKIVRESULTAT = ArkivResultat("42", listOf("666"), false)
        private val FORSIDERESULT = SendResult<Fødselsnummer, MinSideForside>(null, RecordMetadata(TopicPartition("dummyTopic",1),0,0,0,0,0))
        private val RESULT = SendResult<NokkelInput, Any>(null, RecordMetadata(TopicPartition("dummyTopic",1),0,0,0,0,0))
        private val CFG = MinSideConfig(NAISConfig("aap","soknad-api"),
                TopicConfig("beskjed", ofDays(1), true, emptyList(),4),
                TopicConfig("oppgave", ofDays(1), true, emptyList(),4),
                UtkastConfig("utkast", true),
                ForsideConfig("forside",true),
                BacklinksConfig(NAV, NAV, NAV),true, "done")
        private val  FNR = Fødselsnummer("08089403198")

        internal fun ettesending(id: UUID,  type: VedleggType) = StandardEttersending(id, listOf(EttersendtVedlegg(Vedlegg(),type)))
    }
}
internal class InMemoryMellomLager(private val fnr: Fødselsnummer): Mellomlager {

    private val lager = mutableMapOf<Fødselsnummer,String>()

    override fun lagre(value: String, type: SkjemaType) =
        with(fnr) {
            lager[this] = value
            this.fnr
        }

    override fun les(type: SkjemaType) = lager[fnr]

    override fun slett(type: SkjemaType) = lager.remove(fnr) != null

    override fun config(): MellomlagringBucketConfig {
        TODO("Not yet implemented")
    }

}
internal class InMemoryDokumentLager: Dokumentlager {
    private val lager = mutableMapOf<UUID,DokumentInfo>()
    override fun lesDokument(uuid: UUID): DokumentInfo? = lager[uuid]
    override fun slettDokumenter(uuids: List<UUID>) = uuids.forEach { lager.remove(it)}
    override fun slettDokumenter(søknad: StandardSøknad) = lager.clear()

    override fun lagreDokument(dokument: DokumentInfo) =
        with(UUID.randomUUID()) {
            lager[this] = dokument
            this
        }

    override fun slettAlleDokumenter() = lager.clear()
    override fun slettAlleDokumenter(fnr: Fødselsnummer) = slettAlleDokumenter()
}