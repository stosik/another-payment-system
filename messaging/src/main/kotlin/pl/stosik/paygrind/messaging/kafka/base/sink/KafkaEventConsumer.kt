package pl.stosik.paygrind.messaging.kafka.base.sink

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.databind.ObjectMapper
import pl.stosik.paygrind.messaging.kafka.port.driven.KafkaErrorSink
import pl.stosik.paygrind.messaging.kafka.port.driven.KafkaEventSink
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import pl.stosik.paygrind.messaging.kafka.Event
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.util.retry.Retry
import java.time.Duration

internal class KafkaEventConsumer<Value : Event>(
    kafkaConfig: Map<String, Any>,
    private val consumerConfig: KafkaEventConsumerConfig<Value>
) {

    val topic = consumerConfig.topic

    private val receiverOptions = ReceiverOptions
        .create<String, ByteArray>(kafkaConfig)
        .withValueDeserializer(ByteArrayDeserializer())
        .subscription(listOf(consumerConfig.topic))

    suspend fun receive() {
        while (true) {
            log.info { "Starting kafka consumer for topic $topic" }

            KafkaReceiver.create(receiverOptions)
                .receive()
                .retryWhen(
                    Retry.backoff(MAX_RETRIES, Duration.ofMillis(BACKOFF_MILLS))
                )
                .asFlow()
                .collect { record ->
                    log.debug { "Consuming event from kafka topic ${record.topic()}, partition: ${record.partition()}, timestamp ${record.timestamp()}" }

                    Either.catch {
                        consumerConfig.objectMapper.readValue(record.value(), consumerConfig.clazz)
                    }.flatMap {
                        consumerConfig.eventSink.accept(it)
                    }.onLeft {
                        consumerConfig.errorSink.handleError(consumerConfig.topic, it, record)
                    }

                    record
                        .receiverOffset()
                        .commit()
                        .awaitFirstOrNull()
                }
        }
    }

    data class KafkaEventConsumerConfig<Value : Event>(
        val topic: String,
        val clazz: Class<Value>,
        val objectMapper: ObjectMapper,
        val eventSink: KafkaEventSink<Value>,
        val errorSink: KafkaErrorSink
    )

    private companion object {

        private val log = KotlinLogging.logger {}
        private const val MAX_RETRIES = 3L
        private const val BACKOFF_MILLS = 1_000L
    }
}