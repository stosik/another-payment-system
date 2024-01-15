package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.InvoiceStatus

object InvoiceStatusExample {

    fun random() = InvoiceStatus.values().toList().shuffled().first()
}