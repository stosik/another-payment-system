package pl.stosik.paygrind.core.exceptions

abstract class EntityNotFoundException(entity: String, id: Int) :
    Exception("$entity '$id' was not found")
