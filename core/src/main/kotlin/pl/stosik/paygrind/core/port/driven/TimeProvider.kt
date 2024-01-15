package pl.stosik.paygrind.core.port.driven

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}