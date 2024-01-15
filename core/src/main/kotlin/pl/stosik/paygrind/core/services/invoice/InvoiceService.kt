package pl.stosik.paygrind.core.services.invoice

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import pl.stosik.paygrind.data.port.driven.InvoiceFinder
import pl.stosik.paygrind.data.port.driven.InvoiceUpdater
import pl.stosik.paygrind.models.domain.Invoice
import pl.stosik.paygrind.models.domain.InvoiceId
import pl.stosik.paygrind.models.domain.InvoiceStatus
import pl.stosik.paygrind.models.domain.errors.PaygrindError.BillingError.NonRetryableError.InvoiceNotFound

class InvoiceService(
    private val invoiceFinder: InvoiceFinder,
    private val invoiceUpdater: InvoiceUpdater
) {
    fun fetchAll(): List<Invoice> {
        return invoiceFinder.fetchAll()
    }

    fun fetch(id: InvoiceId): Either<InvoiceNotFound, Invoice> = either {
        val invoice = invoiceFinder.fetch(id)
        ensureNotNull(invoice) {
            InvoiceNotFound(id)
        }
    }

    suspend fun fetchPendingInvoices(limit: Int = 100, offset: Long): List<InvoiceId> {
        return invoiceFinder.fetchAllPending(limit, offset)
    }

    suspend fun markInvoiceAsPaid(invoiceId: InvoiceId): Invoice? {
        return invoiceUpdater.updateStatus(invoiceId, InvoiceStatus.PAID)
    }

    suspend fun markInvoiceAsFailed(invoiceId: InvoiceId): Invoice? {
        return invoiceUpdater.updateStatus(invoiceId, InvoiceStatus.FAILED)
    }
}
