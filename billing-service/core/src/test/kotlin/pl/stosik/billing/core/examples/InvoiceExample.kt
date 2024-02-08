package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.Customer
import pl.stosik.billing.models.domain.Invoice
import pl.stosik.billing.models.domain.InvoiceStatus

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