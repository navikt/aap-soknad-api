package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class VedleggEventSubscriber(mapper: ObjectMapper, client: DittNavClient,
                             private val storage: Storage,
                             private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, client, storage, cfgs.vedlegg, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            val resource = mapper.readValue(message.data.toStringUtf8(), Map::class.java)
            log.info("Resource representation: $resource")
            consumer.ack()
        }
}

@ConditionalOnGCP
class MellomlagringEventSubscriber(mapper: ObjectMapper, client: DittNavClient,
                                   private val storage: Storage,
                                   private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, client, storage, cfgs.mellom, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")  // do stuff
            log.info("Data: ${message.attributesMap}")
            val resource = mapper.readValue(message.data.toStringUtf8(), Map::class.java)
            log.info("Resource representation: $resource")
            with(message.attributesMap) {
                when (this["eventType"]) {
                    "OBJECT_FINALIZE" -> {
                        if (message.containsAttributes("overwroteGeneration")) {
                            log.trace("Oppdatert mellomlagring")
                        }
                        else {
                            log.trace("Førstegangs mellomlagring")
                            resource["metadata"]?.let { it as Map<String, String> }?.run {
                                this["uuid"]?.let {
                                    log.info("Oppretter beskjed med UUID $it")
                                    dittNav.opprettBeskjed(STANDARD,
                                            it,
                                            fnr = this["fnr"]!!,
                                            "Du har en påbegynt søknad")
                                } ?: log.warn("Ingen uuid i metadata")
                            } ?: log.warn("Ingen metadata")
                        }
                    }
                    "OBJECT_DELETE" -> {
                        if (message.containsAttributes("overwrittenByGeneration")) {
                            log.trace("Delete pga opppdatert mellomlagring")
                        }
                        else {
                            log.trace("Delete pga avslutt eller timeout")
                            resource["metadata"]?.let { it as Map<String, String> }?.run {
                                this["uuid"]?.let {
                                    log.info("Avslutter beskjed med UUID $it")
                                    dittNav.avsluttBeskjed(STANDARD, fnr = this["fnr"]!!, it)
                                } ?: log.warn("Ingen uuid i metadata")
                            } ?: log.warn("Ingen metadata")
                        }
                    }
                    else -> log.trace("Event type ${this["eventType"]} ikke håndtert")
                }
            }

            consumer.ack()
        }
}