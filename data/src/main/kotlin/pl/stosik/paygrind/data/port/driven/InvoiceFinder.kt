package pl.stosik.paygrind.data.port.driven

import pl.stosik.paygrind.models.domain.Invoice
import pl.stosik.paygrind.models.domain.InvoiceId

interface InvoiceFinder {
    fun fetchAll(): List<Invoice>
    suspend fun fetchAllPending(limit: Int, offset: Long): List<InvoiceId>
    fun fetch(id: InvoiceId): Invoice?
}