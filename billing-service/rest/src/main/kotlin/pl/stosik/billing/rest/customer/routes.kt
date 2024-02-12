package pl.stosik.billing.rest.customer

import arrow.core.right
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.models.domain.Customer
import pl.stosik.billing.models.domain.CustomerId
import pl.stosik.billing.rest.respond

@Resource("/api/v1")
data object RootResource {

    @Resource("customers")
    data class CustomersResource(val parent: RootResource = RootResource) {

        @Resource("{id}")
        data class CustomerResource(val parent: CustomersResource = CustomersResource(), val id: Int)
    }
}

@Serializable
data class SingleCustomerResponse(val id: Int, val currency: String)

@Serializable
data class MultipleCustomersResponse(
    val customers: List<SingleCustomerResponse>,
    val count: Int
)

fun Routing.customerRoutes(service: CustomerService) {
    get<RootResource.CustomersResource> {
        service.fetchAll()
            .let { MultipleCustomersResponse(it.map { it.toResponse() }, it.size) }
            .right()
            .respond(HttpStatusCode.OK)
    }

    get<RootResource.CustomersResource.CustomerResource> { route ->
        service
            .fetch(CustomerId(route.id))
            .map { it.toResponse() }
            .respond(HttpStatusCode.OK)
    }
}

private fun Customer.toResponse() = SingleCustomerResponse(
    id = id.asInt(),
    currency = currency.name
)