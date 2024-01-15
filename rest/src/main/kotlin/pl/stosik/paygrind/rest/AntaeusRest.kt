package pl.stosik.paygrind.rest

import arrow.core.Either
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Context
import pl.stosik.paygrind.core.services.billing.BillingService
import pl.stosik.paygrind.core.services.customer.CustomerService
import pl.stosik.paygrind.core.services.invoice.InvoiceService
import pl.stosik.paygrind.models.domain.CustomerId
import pl.stosik.paygrind.models.domain.InvoiceId
import pl.stosik.paygrind.models.domain.errors.AntaeusError
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.NonRetryableError.*
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.RetryableError.PaymentProviderNetworkError
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

suspend fun ResourceScope.httpServer(
    invoiceService: InvoiceService,
    customerService: CustomerService,
    billingService: BillingService,
): Javalin {
    val app = autoCloseable {
        Javalin
            .create()
            .apply {
                exception(Exception::class.java) { e, _ ->
                    log.error(e) { "Internal server error" }
                }
                error(404) { ctx -> ctx.json("not found") }
            }
    }

    app.routes {
        get("/") {
            it.result("Welcome to Antaeus! see AntaeusRest class for routes")
        }
        path("rest") {
            get("health") {
                it.json("ok")
            }

            // V1
            path("v1") {
                path("invoices") {
                    // URL: /rest/v1/invoices
                    get {
                        it.json(invoiceService.fetchAll())
                    }

                    // URL: /rest/v1/invoices/{:id}
                    get("{id}") { context ->
                        invoiceService
                            .fetch(InvoiceId(context.pathParam("id").toInt()))
                            .toHttpResponse(context)
                    }
                    // URL: /rest/v1/invoices/{:id}/charge
                    path("{id}") {
                        get("/charge") { context ->
                            runBlocking {
                                billingService
                                    .chargeInvoice(InvoiceId(context.pathParam("id").toInt()))
                                    .toHttpResponse(context)
                            }
                        }
                    }
                }

                path("customers") {
                    // URL: /rest/v1/customers
                    get {
                        it.json(customerService.fetchAll())
                    }

                    // URL: /rest/v1/customers/{:id}
                    get("{id}") { context ->
                        customerService
                            .fetch(CustomerId(context.pathParam("id").toInt()))
                            .toHttpResponse(context)
                    }
                }
            }
        }
    }

    return app
}

private fun <Value : Any> Either<AntaeusError, Value>.toHttpResponse(context: Context) =
    mapLeft { it.toHttpStatus() }
        .fold(
            ifLeft = { context.status(it) },
            ifRight = { context.json(it) }
        )

private fun AntaeusError.toHttpStatus(): Int = when (this) {
    is CurrencyMismatch, InsufficientFunds -> 400
    is CustomerNotFound, is InvoiceNotFound -> 404
    is UnknownError, is PaymentProviderNetworkError -> 500
}