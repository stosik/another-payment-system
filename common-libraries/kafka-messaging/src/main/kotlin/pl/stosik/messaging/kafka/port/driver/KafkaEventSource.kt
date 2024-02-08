package pl.stosik.messaging.kafka.port.driver

import arrow.core.Either

interface KafkaEventSource<in Value> {
    suspend fun produce(event: Value): Either<Throwable, Unit>
}