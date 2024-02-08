package pl.stosik.billing.core.services.billing

import pl.stosik.billing.core.infrastracture.adapter.ChargeInvoiceEvent
import pl.stosik.billing.core.infrastracture.adapter.driven.ChargeInvoiceSource
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.models.domain.InvoiceId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BillingProcessor(
    private val invoiceService: InvoiceService,
    private val chargeInvoiceSource: ChargeInvoiceSource
) {

    suspend fun processInvoices(): Unit = coroutineScope {
        fetchPendingInvoicesInBatches()
            .collect {
                launch {
                    chargeInvoiceSource.produce(ChargeInvoiceEvent(it.asInt()))
                }
            }
    }

    private suspend fun fetchPendingInvoicesInBatches(): Flow<InvoiceId> = flow {
        var offset = 0
        do {
            val invoices = invoiceService.fetchPendingInvoices(BATCH_SIZE, offset.toLong())
            emitAll(invoices.asFlow())
            offset += invoices.size
        } while (invoices.size == BATCH_SIZE)
    }

    companion object {

        private const val BATCH_SIZE = 100
    }
}