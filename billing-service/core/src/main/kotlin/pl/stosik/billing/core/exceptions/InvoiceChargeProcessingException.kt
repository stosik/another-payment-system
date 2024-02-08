package pl.stosik.billing.core.exceptions

import pl.stosik.billing.models.domain.errors.PaygrindError

class InvoiceChargeProcessingException(invoiceId: Int, error: PaygrindError) :
    Exception("An error $error occurred while processing the invoice $invoiceId charge.")