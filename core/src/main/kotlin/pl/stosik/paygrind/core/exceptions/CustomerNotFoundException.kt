package pl.stosik.paygrind.core.exceptions

import pl.stosik.paygrind.models.domain.CustomerId

class CustomerNotFoundException(id: CustomerId) : pl.stosik.paygrind.core.exceptions.EntityNotFoundException("Customer", id.asInt())
