package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.storage.Storage
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class VedleggEventSubscriber(mapper: ObjectMapper, private val storage: Storage, private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, storage, cfgs.vedlegg, cfgs.id) {

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
class MellomlagringEventSubscriber(mapper: ObjectMapper,
                                   private val storage: Storage,
                                   private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, storage, cfgs.mellom, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")  // do stuff
            log.info("Data: ${message.attributesMap}")
            val resource = mapper.readValue(message.data.toStringUtf8(), Map::class.java)
            log.info("Resource representation: $resource")
            with(message.attributesMap["eventType"]) {
                when (this) {
                    "OBJECT_FINALIZE" -> {
                        if (message.containsAttributes("overwroteGeneration")) {
                            log.trace("Oppdatert mellomlagring")
                        }
                        else {
                            log.trace("Førstegangs mellomlagring")
                        }
                    }
                    "OBJECT_DELETE" -> {
                        if (message.containsAttributes("overwrittenByGeneration")) {
                            log.trace("Delete pga opppdatert mellomlagring")
                        }
                        else {
                            log.trace("Delete pga avslutt eller timeout")
                        }
                    }
                    else -> log.trace("Event type $this")
                }
            }

            consumer.ack()
        }
}