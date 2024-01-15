package pl.stosik.paygrind.core.services.billing

import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.paygrind.core.examples.CustomerExample
import pl.stosik.paygrind.core.examples.InvoiceExample
import pl.stosik.paygrind.core.port.driven.CurrencyProvider
import pl.stosik.paygrind.core.port.driven.Notifier
import pl.stosik.paygrind.core.port.driven.PaymentProvider
import pl.stosik.paygrind.core.services.customer.CustomerService
import pl.stosik.paygrind.core.services.invoice.InvoiceService
import pl.stosik.paygrind.models.domain.*
import pl.stosik.paygrind.models.domain.errors.PaygrindError
import pl.stosik.paygrind.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CurrencyMismatch
import pl.stosik.paygrind.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CustomerNotFound
import pl.stosik.paygrind.models.domain.errors.PaygrindError.BillingError.RetryableError.PaymentProviderNetworkError
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BillingServiceTest {

    private val customerService = mockk<CustomerService>()
    private val invoiceService = mockk<InvoiceService>(relaxed = true)
    private val paymentProvider = mockk<PaymentProvider>()
    private val currencyProvider = mockk<CurrencyProvider>()
    private val notifier = mockk<Notifier>()

    private val billingService = BillingService(
        customerService = customerService,
        invoiceService = invoiceService,
        paymentProvider = paymentProvider,
        currencyProvider = currencyProvider,
        notifiers = listOf(notifier)
    )

    @Test
    fun `should successfully charge invoice`(): Unit = runTest {
        //given
        val customer = CustomerExample.random()
        val invoice = InvoiceExample.pending(customer)

        every { invoiceService.fetch(invoice.id) } returns invoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        every { paymentProvider.charge(invoice) } returns true
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(invoice.id)

        //then
        result.isRight()
        result.onRight {
            it shouldBe InvoiceCharged(invoice.id)
        }
        coVerify { invoiceService.markInvoiceAsPaid(invoice.id) }
    }

    @Test
    fun `should skip charging not pending invoice`(): Unit = runTest {
        //given
        val customer = CustomerExample.random()
        val paidInvoice = InvoiceExample.pending(customer).copy(status = InvoiceStatus.PAID)

        every { invoiceService.fetch(paidInvoice.id) } returns paidInvoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(paidInvoice.id)

        //then
        result.isRight()
        result.onRight {
            it shouldBe InvoiceCharged(paidInvoice.id)
        }
    }

    @Test
    fun `should fail charging invoice due to payment provider response of insufficient funds`(): Unit = runTest {
        //given
        val customer = CustomerExample.random()
        val invoice = InvoiceExample.pending(customer)

        every { invoiceService.fetch(invoice.id) } returns invoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        every { paymentProvider.charge(invoice) } returns false
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(invoice.id)

        //then
        result.isLeft() shouldBe true
        result.onLeft {
            it shouldBe PaygrindError.BillingError.NonRetryableError.InsufficientFunds
        }
        coVerify { invoiceService.markInvoiceAsFailed(invoice.id) }
    }

    @Test
    fun `should fail charging invoice due to currency mismatch of invoice and customer currency`(): Unit = runTest {
        //given
        val customer = CustomerExample.random().copy(currency = Currency.DKK)
        val invoice = InvoiceExample.pending(customer).copy(amount = Money(BigDecimal.ONE, Currency.EUR))

        every { invoiceService.fetch(invoice.id) } returns invoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(invoice.id)

        //then
        result.isLeft() shouldBe true
        result.onLeft {
            it shouldBe CurrencyMismatch
        }
        coVerify { invoiceService.markInvoiceAsFailed(invoice.id) }
    }

    @Test
    fun `should fail charging invoice due to payment provider network error`(): Unit = runTest {
        //given
        val customer = CustomerExample.random()
        val invoice = InvoiceExample.pending(customer)

        every { invoiceService.fetch(invoice.id) } returns invoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        every { paymentProvider.charge(invoice) } throws pl.stosik.paygrind.core.exceptions.NetworkException()
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(invoice.id)

        //then
        result.isLeft() shouldBe true
        result.onLeft {
            it shouldBe PaymentProviderNetworkError
        }
        verify(exactly = 3) { paymentProvider.charge(invoice) }
        coVerify { invoiceService.markInvoiceAsFailed(invoice.id) }
    }

    @Test
    fun `should fail charging invoice due to customer not found error`(): Unit = runTest {
        //given
        val customer = CustomerExample.random()
        val invoice = InvoiceExample.pending(customer)

        every { invoiceService.fetch(invoice.id) } returns invoice.right()
        every { customerService.fetch(customer.id) } returns customer.right()
        every { paymentProvider.charge(invoice) } throws pl.stosik.paygrind.core.exceptions.CustomerNotFoundException(
            customer.id
        )
        coEvery { notifier.notify(any()) } just runs

        //when
        val result = billingService.chargeInvoice(invoice.id)

        //then
        result.isLeft() shouldBe true
        result.onLeft {
            it shouldBe CustomerNotFound(customer.id)
        }
        coVerify { invoiceService.markInvoiceAsFailed(invoice.id) }
    }

    @Test
    fun `should recover with currency conversion when charging invoice with incompatible currency to customer one`(): Unit =
        runTest {
            //given
            val customer = CustomerExample.random()
            val invoice = InvoiceExample.pending(customer)

            every { invoiceService.fetch(invoice.id) } returns invoice.right()
            every { customerService.fetch(customer.id) } returns customer.right()
            every { currencyProvider.convert(invoice.amount, customer.currency) } returns invoice.amount
            every { paymentProvider.charge(invoice) } throws pl.stosik.paygrind.core.exceptions.CurrencyMismatchException(
                invoice.id,
                customer.id
            ) andThen true
            coEvery { notifier.notify(any()) } just runs

            //when
            val result = billingService.chargeInvoice(invoice.id)

            //then
            result.isRight() shouldBe true
            result.onRight {
                it shouldBe InvoiceCharged(invoice.id)
            }
            coVerify { invoiceService.markInvoiceAsPaid(invoice.id) }
        }
}