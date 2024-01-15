package pl.stosik.paygrind.messaging.kafka

data class KafkaProperties(
    val consumerProperties: Map<String, Any>,
    val producerProperties: Map<String, Any>,
)