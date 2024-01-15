package pl.stosik.paygrind.messaging.kafka.port.driven

import arrow.core.Either
import pl.stosik.paygrind.messaging.kafka.Event

interface KafkaEventSink<Value : Event> {

    suspend fun accept(event: Value): Either<Throwable, Any>
    fun topic(): String
    fun clazz(): Class<Value>
}