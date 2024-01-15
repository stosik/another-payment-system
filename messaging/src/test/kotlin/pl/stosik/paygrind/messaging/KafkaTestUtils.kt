package pl.stosik.paygrind.messaging

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import pl.stosik.paygrind.messaging.IntegrationTestConfig.applicationConfig
import pl.stosik.paygrind.messaging.kafka.KafkaConfiguration
import pl.stosik.paygrind.messaging.kafka.base.json.KafkaJacksonSerializer
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.util.*

object KafkaTestUtils {

    const val TEST_TOPIC = "pl.stosik.paygrind.test.topic"

    private val kafkaProducer = createTestProducer<Any>()

    fun <T : Any> emitEvents(topic: String, vararg events: T) =
        events.map { event ->
            kafkaProducer.send(ProducerRecord(topic, event))
        }

    fun createTestReceiver(topic: String): KafkaReceiver<String, ByteArray> =
        KafkaReceiver.create(
            ReceiverOptions.create<String?, ByteArray>(
                applicationConfig
                    .kafka.consumer
                    .toMap()
                    .joinKeysFlattening() +
                        mapOf(
                            ConsumerConfig.GROUP_INSTANCE_ID_CONFIG to "test-consumer",
                            ConsumerConfig.GROUP_ID_CONFIG to "test-consumer-${UUID.randomUUID()}",
                            ConsumerConfig.CLIENT_ID_CONFIG to "test-consumer-${UUID.randomUUID()}",
                            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to "60000",
                            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to "300000"
                        )
            ).withValueDeserializer(ByteArrayDeserializer())
                .subscription(listOf(topic))
        )

    private fun <V> createTestProducer(): KafkaProducer<String, V> = KafkaProducer(
        applicationConfig.kafka.producer
            .toMap()
            .joinKeysFlattening(),
        StringSerializer(),
        KafkaJacksonSerializer<V>(KafkaConfiguration.objectMapper)
    )
}