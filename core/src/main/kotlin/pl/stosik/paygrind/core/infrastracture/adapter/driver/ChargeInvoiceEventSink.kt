package pl.stosik.paygrind.core.infrastracture.adapter.driver

import arrow.core.Either
import pl.stosik.paygrind.messaging.kafka.port.driven.KafkaEventSink
import pl.stosik.paygrind.models.domain.InvoiceCharged
import pl.stosik.paygrind.models.domain.InvoiceId
import pl.stosik.paygrind.core.infrastracture.adapter.ChargeInvoiceEvent
import pl.stosik.paygrind.core.services.billing.BillingService

class ChargeInvoiceEventSink(
    private val billingService: BillingService
) : KafkaEventSink<ChargeInvoiceEvent> {

    override suspend fun accept(event: ChargeInvoiceEvent): Either<Throwable, InvoiceCharged> {
        return billingService
            .chargeInvoice(InvoiceId(event.invoiceId))
            .mapLeft { pl.stosik.paygrind.core.exceptions.InvoiceChargeProcessingException(event.invoiceId, it) }
    }

    override fun topic(): String = "paygrind.invoice.charge"

    override fun clazz() = ChargeInvoiceEvent::class.java
}