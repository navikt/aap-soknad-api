package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import java.util.concurrent.TimeoutException

object BucketEventSubscriber {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val projectId = "aap-dev-e48b" // TODO fix
        val subscriptionId = "testsub"
        subscribeAsyncExample(projectId, subscriptionId)
    }

    fun subscribeAsyncExample(projectId: String?, subscriptionId: String?) {
        val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)

        // Instantiate an asynchronous message receiver.
        val receiver = MessageReceiver { message: PubsubMessage, consumer: AckReplyConsumer ->
            // Handle incoming message, then ack the received message.
            println("Id: " + message.messageId)
            println("Data: " + message.attributesMap)
            consumer.ack()
        }
        var subscriber: Subscriber? = null
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build()
            // Start the subscriber.
            subscriber.startAsync().awaitRunning()
            System.out.printf("Listening for messages on %s:\n", subscriptionName.toString())
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            subscriber.awaitRunning()
            while (true) {

            }
            //subscriber.awaitTerminated(300, TimeUnit.SECONDS)
        }
        catch (timeoutException: TimeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber!!.stopAsync()
        }
    }
}