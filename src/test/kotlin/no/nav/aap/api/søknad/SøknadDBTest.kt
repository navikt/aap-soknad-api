package no.nav.aap.api.søknad

import java.util.*
import kotlin.test.assertEquals
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.VedleggEtterspørsel
import no.nav.aap.api.søknad.fordeling.SøknadRepository
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.model.VedleggType.*
import no.nav.aap.util.AuthContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DataJpaTest
class DBTest {

    @Autowired
    lateinit var søknadRepo: SøknadRepository
    @MockBean
    lateinit var minSide: MinSideClient

    @MockBean
    lateinit var  arkivClient: ArkivOppslagClient
    @MockBean
    lateinit var ctx: AuthContext
    @Test
    fun testEtterspørrManglende() {
        `when`(ctx.getFnr()).thenReturn(FNR)
        val søknad = SøknadClient(søknadRepo,arkivClient,minSide,ctx)
        val uuid =  UUID.randomUUID()
        søknadRepo.save(Søknad(fnr = FNR.fnr, journalpostid = "42", eventid = uuid))
        søknad.etterspørrVedlegg(VedleggEtterspørsel(FNR, ANNET))
        assertEquals(ANNET, søknad.søknad(uuid)?.manglendeVedlegg?.first())
    }

    companion object  {
        private val  FNR = Fødselsnummer("08089403198")
        @BeforeAll
        @JvmStatic
        internal fun startDB() {
            PostgreSQLContainer<Nothing>("postgres:14:5").apply { start()
            }
        }
    }
}