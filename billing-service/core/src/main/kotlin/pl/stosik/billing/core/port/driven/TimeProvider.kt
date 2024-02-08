package pl.stosik.billing.core.port.driven

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}