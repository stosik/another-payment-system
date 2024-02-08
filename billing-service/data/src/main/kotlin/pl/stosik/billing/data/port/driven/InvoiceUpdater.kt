package pl.stosik.billing.data.port.driven

import pl.stosik.billing.models.domain.Invoice
import pl.stosik.billing.models.domain.InvoiceId
import pl.stosik.billing.models.domain.InvoiceStatus

interface InvoiceUpdater {
    suspend fun updateStatus(id: InvoiceId, status: InvoiceStatus): Invoice?
}