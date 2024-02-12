package pl.stosik.billing.rest.invoice

import arrow.core.right
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.models.domain.*
import pl.stosik.billing.rest.mapping.BigDecimalJson
import pl.stosik.billing.rest.respond

@Resource("/api/v1")
data object RootResource {

    @Resource("invoices")
    data class InvoicesResource(val parent: RootResource = RootResource) {

        @Resource("{id}")
        data class InvoiceResource(val parent: InvoicesResource = InvoicesResource(), val id: Int) {

            @Resource("charge")
            data class ChargeInvoiceResource(val parent: InvoiceResource)
        }
    }
}

@Serializable
data class SingleInvoiceResponse(
    val id: Int,
    val customerId: Int,
    val amount: BigDecimalJson,
    val currency: String,
    val status: String
)

@Serializable
data class MultipleInvoicesResponse(
    val invoices: List<SingleInvoiceResponse>,
    val count: Int
)

@Serializable
data class InvoiceChargedResponse(val invoiceId: Int)

fun Routing.invoiceRoutes(invoiceService: InvoiceService, billingService: BillingService) {
    get<RootResource.InvoicesResource> {
        invoiceService
            .fetchAll()
            .let {
                MultipleInvoicesResponse(it.map { it.toResponse() }, it.size)
            }
            .right()
            .respond(HttpStatusCode.OK)
    }

    get<RootResource.InvoicesResource.InvoiceResource> { route ->
        invoiceService
            .fetch(InvoiceId(route.id))
            .map { it.toResponse() }
            .respond(HttpStatusCode.OK)
    }

    get<RootResource.InvoicesResource.InvoiceResource.ChargeInvoiceResource> { route ->
        billingService
            .chargeInvoice(InvoiceId(route.parent.id))
            .map { InvoiceChargedResponse(it.invoiceId.asInt()) }
            .respond(HttpStatusCode.NoContent)
    }
}

private fun Invoice.toResponse() = SingleInvoiceResponse(
    id = id.asInt(),
    customerId = customerId.asInt(),
    amount = amount.value,
    currency = amount.currency.name,
    status = status.name
)