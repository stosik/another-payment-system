package pl.stosik.billing.core.services.billing

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import pl.stosik.billing.core.extensions.logger.logger
import pl.stosik.billing.core.extensions.resilience.retry
import pl.stosik.billing.core.port.driven.CurrencyProvider
import pl.stosik.billing.core.port.driven.Notifier
import pl.stosik.billing.core.port.driven.PaymentProvider
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.models.domain.Customer
import pl.stosik.billing.models.domain.Invoice
import pl.stosik.billing.models.domain.InvoiceCharged
import pl.stosik.billing.models.domain.InvoiceId
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.NonRetryableError.*
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.RetryableError.PaymentProviderNetworkError
import kotlin.time.Duration.Companion.milliseconds

class BillingService(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val paymentProvider: PaymentProvider,
    private val currencyProvider: CurrencyProvider,
    private val notifiers: List<Notifier>
) {

    private val log by logger()

    suspend fun chargeInvoice(invoiceId: InvoiceId): Either<BillingError, InvoiceCharged> = either {
        log.info { "Invoice with id $invoiceId being processed" }
        val invoice = invoiceService.fetch(invoiceId).bind()
        val customer = customerService.fetch(invoice.customerId).bind()
        if (!invoice.isPending()) InvoiceCharged(invoice.id)
        ensure(customer.currency == invoice.amount.currency) {
            CurrencyMismatch
        }
        charge(invoice, customer).bind()
    }.fold(
        ifLeft = { error ->
            handleError(error, invoiceId)
            error.left()
        },
        ifRight = { invoice ->
            invoiceService.markInvoiceAsPaid(invoice.id)
            log.info { "Invoice ${invoice.id} successfully charged." }
            InvoiceCharged(invoice.id).right()
        }
    )

    private suspend fun charge(invoice: Invoice, customer: Customer): Either<BillingError, Invoice> =
        retry(times = 3, delay = 500.milliseconds, onErrors = arrayOf(PaymentProviderNetworkError)) {
            chargeWithProvider(invoice)
        }.recover {
            when (it) {
                is CurrencyMismatch -> {
                    val convertedAmount = currencyProvider.convert(invoice.amount, customer.currency)
                    val recalculatedInvoice = invoice.recalculate(convertedAmount)
                    chargeWithProvider(recalculatedInvoice).bind()
                }

                else -> raise(it)
            }
        }

    private fun chargeWithProvider(invoice: Invoice): Either<BillingError, Invoice> {
        return Either
            .catch { paymentProvider.charge(invoice) }
            .flatMap { if (it) invoice.right() else InsufficientFunds.left() }
            .mapLeft {
                when (it) {
                    is pl.stosik.billing.core.exceptions.CustomerNotFoundException -> CustomerNotFound(invoice.customerId)
                    is pl.stosik.billing.core.exceptions.CurrencyMismatchException -> CurrencyMismatch
                    is pl.stosik.billing.core.exceptions.NetworkException -> PaymentProviderNetworkError
                    is InsufficientFunds -> InsufficientFunds
                    else -> UnknownError
                }
            }
    }

    private suspend fun handleError(error: BillingError, invoiceId: InvoiceId) {
        when (error) {
            is CurrencyMismatch, is CustomerNotFound, InsufficientFunds, PaymentProviderNetworkError, UnknownError -> {
                invoiceService.markInvoiceAsFailed(invoiceId)
                logAndReportError(invoiceId, error)
            }

            is InvoiceNotFound -> {
                logAndReportError(invoiceId, error)
            }
        }
    }

    private suspend fun logAndReportError(invoiceId: InvoiceId, error: BillingError) {
        log.warn { "Charging invoice with id ${invoiceId.asInt()} failed with error $error" }
        notifiers.forEach { it.notify("Could not charge invoice ${invoiceId.asInt()} due to error: $error. Please inspect and act.") }
    }
}