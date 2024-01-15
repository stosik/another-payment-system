package pl.stosik.paygrind.core.exceptions

import pl.stosik.paygrind.models.domain.errors.PaygrindError

class InvoiceChargeProcessingException(invoiceId: Int, error: PaygrindError) :
    Exception("An error $error occurred while processing the invoice $invoiceId charge.")