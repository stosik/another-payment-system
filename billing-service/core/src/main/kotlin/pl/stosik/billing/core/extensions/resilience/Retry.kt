package pl.stosik.billing.core.extensions.resilience

import arrow.core.Either
import arrow.resilience.Schedule
import pl.stosik.billing.models.domain.errors.PaygrindError
import pl.stosik.billing.models.domain.errors.PaygrindError.BillingError.RetryableError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun <Error : PaygrindError, Success> retry(
    times: Long,
    delay: Duration = 300.milliseconds,
    vararg onErrors: Error,
    block: suspend () -> Either<Error, Success>
): Either<Error, Success> {
    val schedule = Schedule.collect<Either<Error, Success>>() zipLeft Schedule.spaced(delay)
    val last = schedule.doUntil { response, count ->
        val error = unwrapError(response)
        response.isRight()
                || (response.isLeft() && error is RetryableError && error !in onErrors)
                || count.size >= times
    }.repeat(block).last()
    return last
}

private fun <Error : PaygrindError, Success> unwrapError(updateEither: Either<Error, Success>): Error? {
    return updateEither.fold({ error -> error }, { null })
}