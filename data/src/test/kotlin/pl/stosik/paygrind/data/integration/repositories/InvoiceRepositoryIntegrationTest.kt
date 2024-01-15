package pl.stosik.paygrind.data.integration.repositories

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.InvoiceStatus
import pl.stosik.paygrind.models.domain.Money
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.stosik.paygrind.data.integration.IntegrationBase
import java.math.BigDecimal

internal class InvoiceRepositoryIntegrationTest : IntegrationBase() {

    @Test
    fun `should fetch all invoices`() {
        //given
        val customer = createCustomer(Currency.DKK)
        val invoice = createInvoice(
            amount = Money(BigDecimal.TEN, Currency.DKK),
            customer = customer,
            status = InvoiceStatus.PENDING
        )

        //when
        val foundInvoices = invoiceFinder.fetchAll()

        //then
        foundInvoices shouldContainExactlyInAnyOrder listOf(invoice)
    }

    @Test
    fun `should fetch a invoice by id`() {
        //given
        val customer = createCustomer(Currency.DKK)
        val invoice = createInvoice(
            amount = Money(BigDecimal.TEN, Currency.DKK),
            customer = customer,
            status = InvoiceStatus.PENDING
        )

        //when
        val foundInvoice = invoiceFinder.fetch(invoice.id)!!

        //then
        foundInvoice shouldBe invoice
    }

    @Test
    fun `should add a invoice`() {
        //given
        val customer = createCustomer(Currency.DKK)

        //when
        val invoice = createInvoice(
            amount = Money(BigDecimal.TEN, Currency.DKK),
            customer = customer,
            status = InvoiceStatus.PENDING
        )

        //then
        invoice.id shouldNotBe null
        invoice.customerId shouldBe customer.id
        invoice.amount shouldBe Money(BigDecimal.TEN.setScale(2), Currency.DKK)
        invoice.status shouldBe InvoiceStatus.PENDING
    }

    @Test
    fun `should update a invoice`(): Unit = runTest {
        //given
        val customer = createCustomer(Currency.DKK)
        val invoice = createInvoice(
            amount = Money(BigDecimal.TEN, Currency.DKK),
            customer = customer,
            status = InvoiceStatus.PENDING
        )

        //when
        val paidInvoice = invoice.pay()
        val updatedInvoice = invoiceUpdater.updateStatus(paidInvoice.id, InvoiceStatus.PAID)!!

        //then
        updatedInvoice.id shouldBe invoice.id
        updatedInvoice.customerId shouldBe invoice.customerId
        updatedInvoice.amount shouldBe Money(BigDecimal.TEN.setScale(2), Currency.DKK)
        updatedInvoice.status shouldBe InvoiceStatus.PAID
    }
}