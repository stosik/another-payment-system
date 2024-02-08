package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.InvoiceStatus

object InvoiceStatusExample {

    fun random() = InvoiceStatus.values().toList().shuffled().first()
}