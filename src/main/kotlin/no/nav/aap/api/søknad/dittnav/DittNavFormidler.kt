package no.nav.aap.api.søknad.dittnav

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.dittnav.DittNavConfig.DittNavTopicConfig
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.core.env.Environment
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.UUID


@Service
class DittNavFormidler(private val ctx: AuthContext,
                       private val  dittNav: KafkaOperations<NokkelInput, Any>,
                       private val cfg: DittNavConfig,
                       private val env: Environment)  {

    fun opprettBeskjed()  {  // DEV only
        opprettBeskjed(ctx.getFnr())
    }
    fun opprettBeskjed(fnr: Fødselsnummer) = send(fnr,cfg.beskjed)

    private fun send(fnr: Fødselsnummer, cfg: DittNavTopicConfig) =
        with(keyFra(fnr,cfg.grupperingsId)) {
            dittNav.send(ProducerRecord(cfg.topic, this, beskjed(cfg))).addCallback(DittNavCallback(cfg, this))
        }

    private fun beskjed(cfg: DittNavTopicConfig) =
        BeskjedInputBuilder()
            .withSikkerhetsnivaa(3)
            .withTidspunkt(now(UTC))
            .withSynligFremTil(now(UTC).plus(cfg.varighet))
            .withLink(cfg.landingsside)
            .withTekst(cfg.tekst)
            /* .withEksternVarsling(true)
             .withEpostVarslingstekst("AAP-søknad mottat")
             .withEpostVarslingstittel("Tusen takk")
             .withPrefererteKanaler()
             .withSmsVarslingstekst("SMS fra NAV") */
            .build()

    private fun keyFra(fnr: Fødselsnummer, grupperingsId: String) =
        NokkelInputBuilder()
            .withFodselsnummer(fnr.fnr)
            .withEventId(UUID.randomUUID().toString())
            .withGrupperingsId(grupperingsId)
            .withAppnavn(env.getRequiredProperty("nais.app.name"))
            .withNamespace(env.getRequiredProperty("nais.namespace"))
            .build()
    private class DittNavCallback(private val cfg: DittNavTopicConfig, private val key: NokkelInput) :
        ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
        private val log = LoggerUtil.getLogger(javaClass)
        override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
                log.info("Sendte melding ${cfg.tekst} med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på ${cfg.topic}")
            }
            override fun onFailure(e: Throwable) {
                log.warn("Kunne ikke sende melding ${cfg.tekst} med id ${key.getEventId()} på ${cfg.topic}", e)
            }
        }
}