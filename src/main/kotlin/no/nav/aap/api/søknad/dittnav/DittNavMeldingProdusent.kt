package no.nav.aap.api.søknad.dittnav

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
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
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.util.UUID


@Service
class DittNavMeldingProdusent(private val ctx: AuthContext,private val  kafkaOperations: KafkaOperations<NokkelInput, Any>,private val cfg: DittNavConfig,  private val env: Environment)  {
    private val log = LoggerUtil.getLogger(javaClass)

    fun opprettBeskjed(msg: String)  {
        send(msg,nøkkel("AAP-søknad"),cfg.beskjedVarighet,cfg.topics.beskjed)
    }

    private fun send(msg: String, key: NokkelInput, varighet: Duration,topic: String) {
        kafkaOperations.send(ProducerRecord(topic, key, beskjed(msg,URL("http://www.vg.no"),varighet)))
            .addCallback(object : ListenableFutureCallback<SendResult<NokkelInput, Any>?> {
                override fun onSuccess(result: SendResult<NokkelInput, Any>?) {
                    log.info("Sendte melding $msg med id ${key.getEventId()} og offset ${result?.recordMetadata?.offset()} på $topic")
                }
                override fun onFailure(e: Throwable) {
                    log.warn("Kunne ikke sende melding $msg med id ${key.getEventId()} på $topic", e)
                }
            })
    }

    private fun beskjed(tekst: String, landingsside: URL, duration: Duration) =
         BeskjedInputBuilder()
             .withSikkerhetsnivaa(3)
             .withTidspunkt(now(UTC))
             .withSynligFremTil(now(UTC).plus(duration))
             .withLink(landingsside)
             .withTekst(tekst)
            /* .withEksternVarsling(true)
             .withEpostVarslingstekst("AAP-søknad mottat")
             .withEpostVarslingstittel("Tusen takk")
             .withPrefererteKanaler()
             .withSmsVarslingstekst("SMS fra NAV") */
             .build()

    private fun nøkkel(grupperingsId: String?) =

         NokkelInputBuilder()
            .withFodselsnummer(ctx.getFnr().fnr)
            .withEventId(UUID.randomUUID().toString())
            .withGrupperingsId(grupperingsId)
            .withAppnavn(env.getRequiredProperty("nais.app.name"))
            .withNamespace(env.getRequiredProperty("nais.namespace"))
            .build()
}