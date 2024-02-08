package pl.stosik.billing.core.exceptions

import pl.stosik.billing.models.domain.CustomerId

class CustomerNotFoundException(id: CustomerId) : EntityNotFoundException("Customer", id.asInt())
