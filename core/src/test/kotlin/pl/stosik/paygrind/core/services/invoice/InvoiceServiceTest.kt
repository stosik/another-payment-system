package pl.stosik.paygrind.core.services.invoice

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.paygrind.core.examples.InvoiceExample
import pl.stosik.paygrind.data.port.driven.InvoiceFinder
import pl.stosik.paygrind.data.port.driven.InvoiceUpdater
import pl.stosik.paygrind.models.domain.InvoiceId
import pl.stosik.paygrind.models.domain.InvoiceStatus
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.NonRetryableError.InvoiceNotFound

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InvoiceServiceTest {

    private val invoiceFinder = mockk<InvoiceFinder>()
    private val invoiceUpdater = mockk<InvoiceUpdater>()
    private val invoiceService = InvoiceService(invoiceFinder = invoiceFinder, invoiceUpdater = invoiceUpdater)

    @Test
    fun `should return InvoiceNotFound error if invoice is not found`() {
        //given
        every { invoiceFinder.fetch(InvoiceId(404)) } returns null

        //when
        val invoice = invoiceService.fetch(InvoiceId(404))

        //then
        invoice.isLeft() shouldBe true
        invoice.onLeft {
            it shouldBe InvoiceNotFound(InvoiceId(404))
        }
    }

    @Test
    fun `should return invoice when it exists`() {
        //given
        val invoice = InvoiceExample.random()
        every { invoiceFinder.fetch(InvoiceId(404)) } returns invoice

        //when
        val foundInvoice = invoiceService.fetch(InvoiceId(404))

        //then
        foundInvoice.isRight() shouldBe true
        foundInvoice.onRight {
            it shouldBe invoice
        }
    }

    @Test
    fun `should return all invoices`() {
        //given
        val invoice = InvoiceExample.random()
        every { invoiceFinder.fetchAll() } returns listOf(invoice)

        //when
        val foundInvoices = invoiceService.fetchAll()

        //then
        foundInvoices.size shouldBe 1
        foundInvoices[0] shouldBe invoice
    }

    @Test
    fun `should return all pending invoices`(): Unit = runTest {
        //given
        val invoice = InvoiceExample.pending()
        coEvery { invoiceFinder.fetchAllPending(0, 1) } returns listOf(invoice.id)

        //when
        val foundInvoicesIds = invoiceService.fetchPendingInvoices(0, 1)

        //then
        foundInvoicesIds.size shouldBe 1
        foundInvoicesIds[0] shouldBe invoice.id
    }

    @Test
    fun `should mark invoice as paid`(): Unit = runTest {
        //given
        val invoice = InvoiceExample.pending()
        coEvery { invoiceUpdater.updateStatus(InvoiceId(404), InvoiceStatus.PAID) } returns invoice

        //when
        val updatedInvoice = invoiceService.markInvoiceAsPaid(InvoiceId(404))

        //then
        updatedInvoice shouldBe invoice
    }

    @Test
    fun `should mark invoice as failed`(): Unit = runTest {
        //given
        val invoice = InvoiceExample.pending()
        coEvery { invoiceUpdater.updateStatus(InvoiceId(404), InvoiceStatus.FAILED) } returns invoice

        //when
        val updatedInvoice = invoiceService.markInvoiceAsFailed(InvoiceId(404))

        //then
        updatedInvoice shouldBe invoice
    }
}
