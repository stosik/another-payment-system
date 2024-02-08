package pl.stosik.messaging

import java.time.Clock
import java.time.LocalDateTime

internal interface TimeProvider {
    fun now(): LocalDateTime
}

internal object CurrentTimeProvider : TimeProvider {
    private val clock = Clock.systemUTC()

    override fun now(): LocalDateTime = LocalDateTime.now(clock)
}