package pl.stosik.paygrind.e2e.billing

import arrow.fx.coroutines.continuations.resource
import io.kotest.assertions.arrow.fx.coroutines.ProjectResource
import pl.stosik.paygrind.e2e.testDependencies
import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.InvoiceStatus
import pl.stosik.paygrind.models.domain.Money
import kotlinx.coroutines.test.runTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BillingE2ETest {

    private val testDependencies = ProjectResource(resource { testDependencies() })

    @Test
    fun `should bill pending invoices`() = runTest(timeout = 30.seconds) {
        val (invoiceRepository, customerRepository, invoiceService, billingJobScheduler) = testDependencies.get()

        //given
        val customers = (1..5).mapNotNull {
            customerRepository.createCustomer(
                currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
            )
        }

        customers.forEach { customer ->
            (1..10).forEach {
                invoiceRepository.createInvoice(
                    amount = Money(
                        value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                        currency = customer.currency
                    ),
                    customer = customer,
                    status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
                )
            }
        }

        //when
        billingJobScheduler.schedule()

        //then
        await().atMost(30, TimeUnit.SECONDS).until {
            invoiceService.fetchAll().none { it.status == InvoiceStatus.PENDING }
        }
    }
}