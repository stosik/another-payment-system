package pl.stosik.billing.core.infrastracture.adapter.driver

import arrow.core.Either
import pl.stosik.billing.core.exceptions.InvoiceChargeProcessingException
import pl.stosik.billing.core.infrastracture.adapter.ChargeInvoiceEvent
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.models.domain.InvoiceCharged
import pl.stosik.billing.models.domain.InvoiceId
import pl.stosik.messaging.kafka.port.driven.KafkaEventSink

class ChargeInvoiceEventSink(
    private val billingService: BillingService
) : KafkaEventSink<ChargeInvoiceEvent> {

    override suspend fun accept(event: ChargeInvoiceEvent): Either<Throwable, InvoiceCharged> {
        return billingService
            .chargeInvoice(InvoiceId(event.invoiceId))
            .mapLeft { InvoiceChargeProcessingException(event.invoiceId, it) }
    }

    override fun topic(): String = "billing.invoice.charge"

    override fun clazz() = ChargeInvoiceEvent::class.java
}