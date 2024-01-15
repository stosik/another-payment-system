package pl.stosik.paygrind.core.extensions.resilience

import arrow.core.Either
import arrow.resilience.Schedule
import pl.stosik.paygrind.models.domain.errors.AntaeusError
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.RetryableError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun <Error : AntaeusError, Success> retry(
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

private fun <Error : AntaeusError, Success> unwrapError(updateEither: Either<Error, Success>): Error? {
    return updateEither.fold({ error -> error }, { null })
}