package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.Customer
import pl.stosik.paygrind.models.domain.Invoice
import pl.stosik.paygrind.models.domain.InvoiceStatus

object InvoiceExample {

    fun random(customer: Customer? = null) = Invoice(
        id = InvoiceIdExample.random(),
        customerId = customer?.id ?: CustomerIdExample.random(),
        amount = customer?.currency?.let {
            MoneyExample.random().copy(currency = it)
        } ?: MoneyExample.random(),
        status = InvoiceStatusExample.random()
    )

    fun pending(customer: Customer? = null) = Invoice(
        id = InvoiceIdExample.random(),
        customerId = customer?.id ?: CustomerIdExample.random(),
        amount = customer?.currency?.let {
            MoneyExample.random().copy(currency = it)
        } ?: MoneyExample.random(),
        status = InvoiceStatus.PENDING
    )
}