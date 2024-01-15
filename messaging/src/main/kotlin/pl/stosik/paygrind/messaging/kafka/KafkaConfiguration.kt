package pl.stosik.paygrind.messaging.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import pl.stosik.paygrind.messaging.kafka.base.sink.DlqPassingKafkaErrorSink
import pl.stosik.paygrind.messaging.kafka.base.sink.KafkaEventConsumer
import pl.stosik.paygrind.messaging.kafka.base.source.KafkaEventProducer
import pl.stosik.paygrind.messaging.kafka.port.driven.KafkaEventSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG
import pl.stosik.paygrind.messaging.kafka.base.json.KafkaJacksonSerializer
import java.util.*

object KafkaConfiguration {

    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    private val kafkaClientId = UUID.randomUUID()

    private lateinit var kafkaConfig: KafkaProperties

    private val dlqSink by lazy {
        DlqPassingKafkaErrorSink(
            objectMapper,
            kafkaConfig.producerProperties + mapOf(
                CLIENT_ID_CONFIG to "dlq-producer-$kafkaClientId"
            )
        )
    }

    context(arrow.fx.coroutines.continuations.ResourceScope) suspend fun <Value : Event> configure(
        consumerConfig: Map<String, Any>,
        producerConfig: Map<String, Any>,
        sinks: List<KafkaEventSink<Value>>,
    ): KafkaConfiguration {
        val scope = coroutineScope(Dispatchers.IO)
        kafkaConfig = KafkaProperties(
            consumerProperties = consumerConfig,
            producerProperties = producerConfig
        )
        sinks.map {
            createKafkaConsumer(
                topic = it.topic(),
                clazz = it.clazz(),
                eventSink = it
            )
        }.map { consumer ->
            scope.launch(MDCContext(mapOf("topic" to consumer.topic))) {
                consumer.receive()
            }
        }

        return this
    }

    fun <Value : Event> createProducer(topic: String): KafkaEventProducer<Value> {
        return createKafkaProducer(
            topic = topic,
            valueSerializer = KafkaJacksonSerializer(objectMapper)
        )
    }

    private fun <Value : Event> createKafkaConsumer(
        topic: String,
        clazz: Class<Value>,
        eventSink: KafkaEventSink<Value>,
    ) = KafkaEventConsumer(
        kafkaConfig.consumerProperties +
                mapOf(
                    CommonClientConfigs.GROUP_INSTANCE_ID_CONFIG to "$topic-$kafkaClientId",
                    CLIENT_ID_CONFIG to "topic-consumer-$kafkaClientId"
                ),
        KafkaEventConsumer.KafkaEventConsumerConfig(
            topic = topic,
            clazz = clazz,
            objectMapper = objectMapper,
            eventSink = eventSink,
            errorSink = dlqSink
        )
    )

    private fun <Value : Event> createKafkaProducer(
        topic: String,
        valueSerializer: KafkaJacksonSerializer<Value>,
    ) = KafkaEventProducer(
        kafkaConfig.producerProperties +
                mapOf(
                    CommonClientConfigs.GROUP_INSTANCE_ID_CONFIG to "$topic-$kafkaClientId",
                    CLIENT_ID_CONFIG to "topic-producer-$kafkaClientId"
                ),
        KafkaEventProducer.KafkaEventProducerConfig(
            topic,
            valueSerializer
        )
    )
}