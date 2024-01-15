package pl.stosik.paygrind.core.exceptions

import pl.stosik.paygrind.models.domain.errors.AntaeusError

class InvoiceChargeProcessingException(invoiceId: Int, error: AntaeusError) :
    Exception("An error $error occurred while processing the invoice $invoiceId charge.")