package pl.stosik.billing.data.integration.repositories

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import pl.stosik.billing.models.domain.Currency
import org.junit.jupiter.api.Test
import pl.stosik.billing.data.integration.IntegrationBase

internal class CustomerRepositoryIntegrationTest : IntegrationBase() {

    @Test
    fun `should add a customer`() {
        val customer = createCustomer(Currency.DKK)

        customer.id shouldNotBe null
        customer.currency shouldBe Currency.DKK
    }

    @Test
    fun `should fetch all customers`() {
        val customer = createCustomer(Currency.DKK)

        val foundCustomers = customerFinder.fetchAll()

        foundCustomers shouldContainExactlyInAnyOrder listOf(customer)
    }

    @Test
    fun `should fetch a customer by id`() {
        val customer = createCustomer(Currency.DKK)

        val foundCustomer = customerFinder.fetch(customer.id)!!

        foundCustomer.id shouldBe customer.id
        foundCustomer.currency shouldBe customer.currency
    }
}