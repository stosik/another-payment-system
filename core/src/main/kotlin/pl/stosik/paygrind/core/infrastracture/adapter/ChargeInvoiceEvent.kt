package pl.stosik.paygrind.core.infrastracture.adapter

import pl.stosik.paygrind.messaging.kafka.Event

data class ChargeInvoiceEvent(val invoiceId: Int) : Event {

    override val metadataId: String
        get() = invoiceId.toString()
}