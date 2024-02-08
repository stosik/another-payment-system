package pl.stosik.billing.data.port.driven

import pl.stosik.billing.models.domain.Invoice
import pl.stosik.billing.models.domain.InvoiceId

interface InvoiceFinder {
    fun fetchAll(): List<Invoice>
    suspend fun fetchAllPending(limit: Int, offset: Long): List<InvoiceId>
    fun fetch(id: InvoiceId): Invoice?
}