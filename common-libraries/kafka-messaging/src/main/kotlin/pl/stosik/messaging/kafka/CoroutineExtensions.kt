package pl.stosik.messaging.kafka

import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.ResourceScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

suspend fun ResourceScope.coroutineScope(context: CoroutineContext): CoroutineScope =
    install({ CoroutineScope(context) }, { scope, exitCase ->
        when (exitCase) {
            ExitCase.Completed -> scope.cancel()
            is ExitCase.Cancelled -> scope.cancel(exitCase.exception)
            is ExitCase.Failure -> scope.cancel("Resource failed, so cancelling associated scope", exitCase.failure)
        }
        scope.coroutineContext.job.join()
    })