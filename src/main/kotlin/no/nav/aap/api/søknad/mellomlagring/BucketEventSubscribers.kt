package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.storage.Storage
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class VedleggEventSubscriber(private val storage: Storage, private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(storage, cfgs.vedlegg, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
}

@ConditionalOnGCP
class MellomlagringEventSubscriber(private val storage: Storage, private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(storage, cfgs.mellom, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
}