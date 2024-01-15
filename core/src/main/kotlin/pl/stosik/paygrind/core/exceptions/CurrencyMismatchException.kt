package pl.stosik.paygrind.core.exceptions

import pl.stosik.paygrind.models.domain.CustomerId
import pl.stosik.paygrind.models.domain.InvoiceId

class CurrencyMismatchException(invoiceId: InvoiceId, customerId: CustomerId) :
    Exception("Currency of invoice '${invoiceId.asInt()}' does not match currency of customer '${customerId.asInt()}'")
