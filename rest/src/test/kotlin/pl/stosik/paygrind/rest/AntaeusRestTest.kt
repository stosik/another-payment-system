package pl.stosik.paygrind.rest

import arrow.core.left
import arrow.core.right
import arrow.fx.coroutines.continuations.resource
import io.javalin.json.JavalinJackson
import io.javalin.testtools.JavalinTest
import io.kotest.assertions.arrow.fx.coroutines.ProjectResource
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import pl.stosik.paygrind.core.services.billing.BillingService
import pl.stosik.paygrind.core.services.customer.CustomerService
import pl.stosik.paygrind.core.services.invoice.InvoiceService
import pl.stosik.paygrind.models.domain.*
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.NonRetryableError.*
import pl.stosik.paygrind.rest.httpServer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AntaeusRestTest {

    private val mapper = JavalinJackson()
    private val customerService = mockk<CustomerService>()
    private val invoiceService = mockk<InvoiceService>()
    private val billingService = mockk<BillingService>()

    private val app = ProjectResource(
        resource {
            httpServer(
                customerService = customerService,
                invoiceService = invoiceService,
                billingService = billingService
            )
        }
    )

    @Test
    fun `should GET healthcheck message`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given

            //when
            val response = client.get("/")

            //then
            response.code shouldBe 200
            response.body?.string() shouldBe "Welcome to Antaeus! see AntaeusRest class for routes"
        }
    }

    @Test
    fun `should GET all invoices`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val invoice =
                Invoice(InvoiceId(1), CustomerId(1), Money(BigDecimal.ONE, Currency.DKK), InvoiceStatus.PENDING)
            every { invoiceService.fetchAll() } returns listOf(invoice)

            //when
            val response = client.get("/rest/v1/invoices")

            //then
            response.code shouldBe 200
            response.body?.string() shouldBe mapper.toJsonString(obj = listOf(invoice), type = List::class.java)
        }
    }

    @Test
    fun `should GET invoice by id`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val invoice =
                Invoice(InvoiceId(1), CustomerId(1), Money(BigDecimal.ONE, Currency.DKK), InvoiceStatus.PENDING)
            every { invoiceService.fetch(invoice.id) } returns invoice.right()

            //when
            val response = client.get("/rest/v1/invoices/${invoice.id.asInt()}")

            //then
            response.code shouldBe 200
            response.body?.string() shouldBe mapper.toJsonString(obj = invoice, type = Invoice::class.java)
        }
    }

    @Test
    fun `should return 404 when invoice does not exist`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val invoice =
                Invoice(InvoiceId(1), CustomerId(1), Money(BigDecimal.ONE, Currency.DKK), InvoiceStatus.PENDING)
            every { invoiceService.fetch(invoice.id) } returns InvoiceNotFound(invoice.id).left()

            //when
            val response = client.get("/rest/v1/invoices/${invoice.id.asInt()}")

            //then
            response.code shouldBe 404
        }
    }

    @Test
    fun `should charge invoice by id`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val invoice =
                Invoice(InvoiceId(1), CustomerId(1), Money(BigDecimal.ONE, Currency.DKK), InvoiceStatus.PENDING)
            coEvery { billingService.chargeInvoice(invoice.id) } returns InvoiceCharged(invoice.id).right()

            //when
            val response = client.get("/rest/v1/invoices/${invoice.id}/charge")

            //then
            response.code shouldBe 200
        }
    }

    @Test
    fun `should return 400 when charging invoice with incompatible currency`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val invoice =
                Invoice(InvoiceId(1), CustomerId(1), Money(BigDecimal.ONE, Currency.DKK), InvoiceStatus.PENDING)
            coEvery { billingService.chargeInvoice(invoice.id) } returns CurrencyMismatch.left()

            //when
            val response = client.get("/rest/v1/invoices/${invoice.id.asInt()}/charge")

            //then
            response.code shouldBe 400
        }
    }

    @Test
    fun `should GET all customers`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val customer = Customer(CustomerId(1), Currency.DKK)
            every { customerService.fetchAll() } returns listOf(customer)

            //when
            val response = client.get("/rest/v1/customers")

            //then
            response.code shouldBe 200
            response.body?.string() shouldBe mapper.toJsonString(obj = listOf(customer), type = List::class.java)
        }
    }

    @Test
    fun `should GET customer by id`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val customer = Customer(CustomerId(1), Currency.DKK)
            every { customerService.fetch(customer.id) } returns customer.right()

            //when
            val response = client.get("/rest/v1/customers/${customer.id.asInt()}")

            //then
            response.code shouldBe 200
            response.body?.string() shouldBe mapper.toJsonString(obj = customer, type = Customer::class.java)
        }
    }

    @Test
    fun `should return 404 when customer does not exist`() = runTest {
        JavalinTest.test(app.get()) { _, client ->
            //given
            val customer = Customer(CustomerId(1), Currency.DKK)
            every { customerService.fetch(customer.id) } returns CustomerNotFound(customer.id).left()

            //when
            val response = client.get("/rest/v1/customers/${customer.id.asInt()}")

            //then
            response.code shouldBe 404
        }
    }
}