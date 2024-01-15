package pl.stosik.paygrind.core.infrastracture.adapter.driven

import arrow.core.Either
import pl.stosik.paygrind.core.infrastracture.adapter.ChargeInvoiceEvent
import pl.stosik.paygrind.messaging.kafka.base.source.KafkaEventProducer
import pl.stosik.paygrind.messaging.kafka.port.driver.KafkaEventSource

class ChargeInvoiceSource(
    private val producer: KafkaEventProducer<ChargeInvoiceEvent>
) : KafkaEventSource<ChargeInvoiceEvent> {

    override suspend fun produce(event: ChargeInvoiceEvent): Either<Throwable, Unit> = producer.produce(event)

    companion object {
        const val TOPIC = "paygrind.invoice.charge"
    }
}