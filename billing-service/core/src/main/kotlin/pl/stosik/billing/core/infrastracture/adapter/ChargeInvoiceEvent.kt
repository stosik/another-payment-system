package pl.stosik.billing.core.infrastracture.adapter

import pl.stosik.messaging.kafka.Event

data class ChargeInvoiceEvent(val invoiceId: Int) : Event {

    override val metadataId: String
        get() = invoiceId.toString()
}