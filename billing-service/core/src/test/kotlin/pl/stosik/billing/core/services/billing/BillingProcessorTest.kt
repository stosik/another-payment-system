package pl.stosik.billing.core.services.billing

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.billing.core.examples.InvoiceExample
import pl.stosik.billing.core.infrastracture.adapter.ChargeInvoiceEvent
import pl.stosik.billing.core.infrastracture.adapter.driven.ChargeInvoiceSource
import pl.stosik.billing.core.services.invoice.InvoiceService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BillingProcessorTest {

    private val invoiceService = mockk<InvoiceService>()
    private val chargeInvoiceSource = mockk<ChargeInvoiceSource>(relaxed = true)

    private val billingProcessor = BillingProcessor(
        invoiceService = invoiceService,
        chargeInvoiceSource = chargeInvoiceSource
    )

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should process pending invoices when there is at least one pending invoice`(): Unit = runTest {
        //given
        val invoice = InvoiceExample.pending()
        coEvery { invoiceService.fetchPendingInvoices(100, 0) } returns listOf(invoice.id)

        //when
        billingProcessor.processInvoices()

        //then
        coVerify(exactly = 1) { chargeInvoiceSource.produce(ChargeInvoiceEvent(invoice.id.asInt())) }
    }

    @Test
    fun `should not process any invoice when there are no pending invoices`(): Unit = runTest {
        //given
        coEvery { invoiceService.fetchPendingInvoices(100, 0) } returns emptyList()

        //when
        billingProcessor.processInvoices()

        //then
        coVerify { chargeInvoiceSource wasNot called }
    }
}