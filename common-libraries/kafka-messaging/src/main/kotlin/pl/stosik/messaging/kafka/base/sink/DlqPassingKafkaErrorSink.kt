package pl.stosik.messaging.kafka.base.sink

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import pl.stosik.messaging.CurrentTimeProvider
import pl.stosik.messaging.kafka.base.json.KafkaJacksonSerializer
import pl.stosik.messaging.kafka.port.driven.KafkaErrorSink
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.time.LocalDateTime

internal class DlqPassingKafkaErrorSink(
    objectMapper: ObjectMapper,
    config: Map<String, Any>,
) : KafkaErrorSink {

    private val senderOptions = SenderOptions
        .create<String, DlqMessage>(config)
        .withValueSerializer(KafkaJacksonSerializer<DlqMessage>(objectMapper))

    private val sender = KafkaSender.create(senderOptions)

    override suspend fun handleError(topic: String, error: Throwable, record: ReceiverRecord<String, ByteArray>) {
        log.error(
            "Encountered error while handling event in topic $topic. " +
                    "Sending event to DLQ $TOPIC. Error cause: ${error.message}",
            error
        )

        val senderRecord = SenderRecord.create(
            ProducerRecord(
                TOPIC,
                null,
                record.key(),
                DlqMessage(
                    originalTopic = topic,
                    timestamp = CurrentTimeProvider.now(),
                    originalMessage = String(record.value()),
                ),
                record.headers()
            ),
            Unit,
        )

        sender
            .send(flowOf(senderRecord).asPublisher(Dispatchers.IO))
            .awaitFirstOrNull()
    }

    data class DlqMessage(
        val originalTopic: String,
        val timestamp: LocalDateTime,
        val originalMessage: String,
    )

    companion object {
        private val log = KotlinLogging.logger { }

        const val TOPIC = "billing.events.dlq"
    }
}