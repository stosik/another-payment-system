package pl.stosik.messaging.kafka

data class KafkaProperties(
    val consumerProperties: Map<String, Any>,
    val producerProperties: Map<String, Any>,
)