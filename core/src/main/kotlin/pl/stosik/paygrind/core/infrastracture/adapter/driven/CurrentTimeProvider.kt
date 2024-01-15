package pl.stosik.paygrind.core.infrastracture.adapter.driven

import pl.stosik.paygrind.core.port.driven.TimeProvider
import java.time.Clock
import java.time.LocalDateTime

class CurrentTimeProvider : TimeProvider {
    private val clock = Clock.systemUTC()

    override fun now(): LocalDateTime = LocalDateTime.now(clock)
}