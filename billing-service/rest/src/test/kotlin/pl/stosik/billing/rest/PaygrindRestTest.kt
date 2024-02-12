package pl.stosik.billing.rest

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.models.domain.Currency
import pl.stosik.billing.models.domain.Customer
import pl.stosik.billing.models.domain.CustomerId
import pl.stosik.billing.models.domain.Invoice
import pl.stosik.billing.models.domain.InvoiceCharged
import pl.stosik.billing.models.domain.InvoiceId
import pl.stosik.billing.models.domain.InvoiceStatus
import pl.stosik.billing.models.domain.Money
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CurrencyMismatch
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CustomerNotFound
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.InvoiceNotFound
import pl.stosik.billing.rest.customer.MultipleCustomersResponse
import pl.stosik.billing.rest.customer.SingleCustomerResponse
import pl.stosik.billing.rest.customer.customerRoutes
import pl.stosik.billing.rest.invoice.MultipleInvoicesResponse
import pl.stosik.billing.rest.invoice.SingleInvoiceResponse
import pl.stosik.billing.rest.invoice.invoiceRoutes
import java.math.BigDecimal

internal class PaygrindRestTest {

    private val customerService = mockk<CustomerService>()
    private val invoiceService = mockk<InvoiceService>()
    private val billingService = mockk<BillingService>()

    @Test
    fun `should GET all invoices`(): Unit = invoices {
        //given
        val invoice = Invoice(
            InvoiceId(1),
            CustomerId(1),
            Money(BigDecimal.ONE, Currency.DKK),
            InvoiceStatus.PENDING
        )
        every { invoiceService.fetchAll() } returns listOf(invoice)

        //when
        val response = get("/api/v1/invoices")

        //then
        response.status shouldBe OK
        response.body<MultipleInvoicesResponse>() shouldBe MultipleInvoicesResponse(
            invoices = listOf(invoice.toResponse()),
            count = 1
        )
    }

    @Test
    fun `should GET invoice by id`() = invoices {
        //given
        val invoice = Invoice(
            InvoiceId(1),
            CustomerId(1),
            Money(BigDecimal.ONE, Currency.DKK),
            InvoiceStatus.PENDING
        )
        every { invoiceService.fetch(invoice.id) } returns invoice.right()

        //when
        val response = get("/api/v1/invoices/${invoice.id.asInt()}")

        //then
        response.status shouldBe OK
        response.body<SingleInvoiceResponse>() shouldBe invoice.toResponse()
    }

    @Test
    fun `should return 404 when invoice does not exist`() = invoices {
        //given
        val invoice = Invoice(
            InvoiceId(1),
            CustomerId(1),
            Money(BigDecimal.ONE, Currency.DKK),
            InvoiceStatus.PENDING
        )
        every { invoiceService.fetch(invoice.id) } returns InvoiceNotFound(invoice.id).left()

        //when
        val response = get("/api/v1/invoices/${invoice.id.asInt()}")

        //then
        response.status shouldBe NotFound
    }

    @Test
    fun `should charge invoice by id`() = invoices {
        //given
        val invoice = Invoice(
            InvoiceId(1),
            CustomerId(1),
            Money(BigDecimal.ONE, Currency.DKK),
            InvoiceStatus.PENDING
        )
        coEvery { billingService.chargeInvoice(invoice.id) } returns InvoiceCharged(invoice.id).right()

        //when
        val response = get("/api/v1/invoices/${invoice.id.asInt()}/charge")

        //then
        response.status shouldBe NoContent
    }

    @Test
    fun `should return 400 when charging invoice with incompatible currency`() = invoices {
        //given
        val invoice = Invoice(
            InvoiceId(1),
            CustomerId(1),
            Money(BigDecimal.ONE, Currency.DKK),
            InvoiceStatus.PENDING
        )
        coEvery { billingService.chargeInvoice(invoice.id) } returns CurrencyMismatch.left()

        //when
        val response = get("/api/v1/invoices/${invoice.id.asInt()}/charge")

        //then
        response.status shouldBe BadRequest
    }

    @Test
    fun `should GET all customers`() = customers {
        //given
        val customer = Customer(CustomerId(1), Currency.DKK)
        every { customerService.fetchAll() } returns listOf(customer)

        //when
        val response = get("/api/v1/customers")

        //then
        response.status shouldBe OK
        response.body<MultipleCustomersResponse>() shouldBe MultipleCustomersResponse(
            customers = listOf(customer.toResponse()),
            count = 1
        )
    }

    @Test
    fun `should GET customer by id`() = customers {
        //given
        val customer = Customer(CustomerId(1), Currency.DKK)
        every { customerService.fetch(customer.id) } returns customer.right()

        //when
        val response = get("/api/v1/customers/${customer.id.asInt()}")

        //then
        response.status shouldBe OK
        response.body<SingleCustomerResponse>() shouldBe customer.toResponse()
    }

    @Test
    fun `should return 404 when customer does not exist`() = customers {
        //given
        val customer = Customer(CustomerId(1), Currency.DKK)
        every { customerService.fetch(customer.id) } returns CustomerNotFound(customer.id).left()

        //when
        val response = get("/api/v1/customers/${customer.id.asInt()}")

        //then
        response.status shouldBe NotFound
    }

    private fun <A> customers(test: suspend HttpClient.() -> A) = runTest {
        testApp({
            routing { customerRoutes(customerService) }
        }, test)
    }

    private fun <A> invoices(test: suspend HttpClient.() -> A) = runTest {
        testApp({
            routing { invoiceRoutes(invoiceService, billingService) }
        }, test)
    }
}

private fun Invoice.toResponse() = SingleInvoiceResponse(
    id = id.asInt(),
    customerId = customerId.asInt(),
    amount = amount.value,
    currency = amount.currency.name,
    status = status.name
)

private fun Customer.toResponse() = SingleCustomerResponse(
    id = id.asInt(),
    currency = currency.name
)