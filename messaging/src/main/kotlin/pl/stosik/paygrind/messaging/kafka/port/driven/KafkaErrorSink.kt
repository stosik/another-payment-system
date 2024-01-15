package pl.stosik.paygrind.messaging.kafka.port.driven

import reactor.kafka.receiver.ReceiverRecord

interface KafkaErrorSink {

    suspend fun handleError(topic: String, error: Throwable, record: ReceiverRecord<String, ByteArray>)
}