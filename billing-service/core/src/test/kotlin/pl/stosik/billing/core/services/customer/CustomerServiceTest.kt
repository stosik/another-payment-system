package pl.stosik.billing.core.services.customer

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.billing.core.examples.CustomerExample
import pl.stosik.billing.data.port.driven.CustomerFinder
import pl.stosik.billing.models.domain.CustomerId
import pl.stosik.billing.models.domain.errors.PaygrindError

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CustomerServiceTest {

    private val customerFinder = mockk<CustomerFinder>()
    private val customerService = CustomerService(customerFinder = customerFinder)

    @Test
    fun `should return CustomerNotFound error if customer is not found`() {
        //given
        every { customerFinder.fetch(CustomerId(404)) } returns null

        //when
        val customer = customerService.fetch(CustomerId(404))

        //then
        customer.isLeft() shouldBe true
        customer.onLeft {
            it shouldBe PaygrindError.BillingError.NonRetryableError.CustomerNotFound(CustomerId(404))
        }
    }

    @Test
    fun `should return customer when it exists`() {
        //given
        val customer = CustomerExample.random()
        every { customerFinder.fetch(CustomerId(404)) } returns customer

        //when
        val foundCustomer = customerService.fetch(CustomerId(404))

        //then
        foundCustomer.isRight() shouldBe true
        foundCustomer.onRight {
            it shouldBe customer
        }
    }

    @Test
    fun `should return all customers`() {
        //given
        val customer = CustomerExample.random()
        every { customerFinder.fetchAll() } returns listOf(customer)

        //when
        val foundCustomers = customerService.fetchAll()

        //then
        foundCustomers.size shouldBe 1
        foundCustomers[0] shouldBe customer
    }
}
