package pl.stosik.billing.rest

import arrow.core.Either
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.ktor.Cohort
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.models.domain.errors.PaygrindError
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CurrencyMismatch
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CustomerNotFound
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.InsufficientFunds
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.InvoiceNotFound
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.UnknownError
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.RetryableError.PaymentProviderNetworkError
import pl.stosik.billing.rest.customer.customerRoutes
import pl.stosik.billing.rest.invoice.invoiceRoutes
import pl.stosik.billing.rest.metrics.metricsRoute

fun Application.billingServer(
    invoiceService: InvoiceService,
    customerService: CustomerService,
    billingService: BillingService,
    healthCheck: HealthCheckRegistry,
    metricsRegistry: PrometheusMeterRegistry
) {
    configure()
    healthChecks(healthCheck)
    metrics(metricsRegistry)
    routes(invoiceService, customerService, billingService, metricsRegistry)
}

fun Application.configure() {
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json(
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(Resources)
}

private fun Application.healthChecks(healthCheck: HealthCheckRegistry) {
    install(Cohort) {
        healthcheck("/readiness", healthCheck)
        healthcheck("/health", healthCheck)
    }
}

private fun Application.metrics(metricsRegistry: PrometheusMeterRegistry) {
    install(MicrometerMetrics) {
        registry = metricsRegistry
    }
}

private fun Application.routes(
    invoiceService: InvoiceService,
    customerService: CustomerService,
    billingService: BillingService,
    metricsRegistry: PrometheusMeterRegistry
): Routing =
    routing {
        metricsRoute(metricsRegistry)
        customerRoutes(customerService)
        invoiceRoutes(invoiceService, billingService)
    }

context(PipelineContext<Unit, ApplicationCall>) suspend inline fun <reified A : Any> Either<PaygrindError, A>.respond(
    status: HttpStatusCode
): Unit =
    when (this) {
        is Either.Left -> respondError(value)
        is Either.Right -> call.respond(status, value)
    }

suspend fun PipelineContext<Unit, ApplicationCall>.respondError(error: PaygrindError): Unit =
    when (error) {
        is CurrencyMismatch, InsufficientFunds -> call.respond(HttpStatusCode.BadRequest)
        is CustomerNotFound, is InvoiceNotFound -> call.respond(HttpStatusCode.NotFound)
        is UnknownError, is PaymentProviderNetworkError -> call.respond(HttpStatusCode.InternalServerError)
    }