package pl.stosik.paygrind.data.port.driven

import pl.stosik.paygrind.models.domain.Invoice
import pl.stosik.paygrind.models.domain.InvoiceId
import pl.stosik.paygrind.models.domain.InvoiceStatus

interface InvoiceUpdater {
    suspend fun updateStatus(id: InvoiceId, status: InvoiceStatus): Invoice?
}