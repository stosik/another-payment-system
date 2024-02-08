package pl.stosik.messaging.kafka.base.source

import arrow.core.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import pl.stosik.messaging.kafka.Event
import pl.stosik.messaging.kafka.base.json.KafkaJacksonSerializer
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord

class KafkaEventProducer<Value : Event>(
    config: Map<String, Any>,
    private val producerConfig: KafkaEventProducerConfig<Value>
) {

    private val senderOptions = SenderOptions
        .create<String, Value>(config)
        .withValueSerializer(producerConfig.valueSerializer)

    private val sender = KafkaSender.create(senderOptions)

    private val ProducerRecord<*, *>.debugInfo: String
        get() = "[topic:${topic()}]," +
                " [partition:${partition()}]," +
                " [timestamp:${timestamp()}]," +
                " [key:${key()}]," +
                " [value:${value()}]"


    suspend fun produce(event: Value): Either<Throwable, Unit> = coroutineScope {
        val producerRecord = ProducerRecord<String, Value>(
            producerConfig.topic,
            event
        )

        val senderRecord = SenderRecord.create(
            producerRecord,
            "${producerConfig.topic}-${event.metadataId}"
        )

        Either.catch {
            sender.send(flowOf(senderRecord).asPublisher(Dispatchers.IO))
                .doOnError { log.error("Error while sending event ${producerRecord.debugInfo}: ${it.message}") }
                .asFlow()
                .collect { log.info("The event has been sent successfully {}", it) }
        }
    }

    data class KafkaEventProducerConfig<Value : Event>(
        val topic: String,
        val valueSerializer: KafkaJacksonSerializer<Value>
    )

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}