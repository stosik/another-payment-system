package pl.stosik.billing.models.domain.errors

import pl.stosik.billing.models.domain.CustomerId
import pl.stosik.billing.models.domain.InvoiceId

sealed class PaygrindError {
    sealed class BillingError : PaygrindError() {
        sealed class RetryableError : BillingError() {
            data object PaymentProviderNetworkError : RetryableError()
        }

        sealed class NonRetryableError : BillingError() {
            data class CustomerNotFound(val id: CustomerId) : NonRetryableError()
            data class InvoiceNotFound(val id: InvoiceId) : NonRetryableError()
            data object InsufficientFunds : NonRetryableError()
            data object CurrencyMismatch : NonRetryableError()
            data object UnknownError : NonRetryableError()
        }
    }
}