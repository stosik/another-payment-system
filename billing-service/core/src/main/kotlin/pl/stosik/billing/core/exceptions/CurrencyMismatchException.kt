package pl.stosik.billing.core.exceptions

import pl.stosik.billing.models.domain.CustomerId
import pl.stosik.billing.models.domain.InvoiceId

class CurrencyMismatchException(invoiceId: InvoiceId, customerId: CustomerId) :
    Exception("Currency of invoice '${invoiceId.asInt()}' does not match currency of customer '${customerId.asInt()}'")
