package pl.stosik.paygrind.data

import arrow.core.Either
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.exception.*
import org.jooq.impl.DSL
import pl.stosik.paygrind.data.configuration.JooqConfigurationElement
import pl.stosik.paygrind.data.exception.*
import reactor.core.publisher.Flux.from
import kotlin.coroutines.coroutineContext


class JooqEngine(private val dsl: DSLContext) {

    init {
        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")
    }

    suspend fun <R> query(block: suspend DSLContext.() -> R): Either<PersistenceException, R> {
        return withExceptionHandling {
            block(DSL.using(configuration()))
        }
    }

    suspend fun <R> transaction(block: suspend DSLContext.() -> R): Either<PersistenceException, R> {
        return withExceptionHandling {
            DSL.using(configuration()).transactionPublisher { t ->
                from(
                    mono {
                        withContext(JooqConfigurationElement(t)) {
                            block(DSL.using(t))
                        }
                    }
                )
            }.awaitLast()
        }
    }

    private suspend fun configuration(): Configuration {
        val jooqElement = coroutineContext[JooqConfigurationElement]
        return jooqElement?.value ?: dsl.configuration()
    }

    private suspend fun <R> withExceptionHandling(block: suspend () -> R): Either<PersistenceException, R> {
        return Either.catch { block() }.mapLeft { mapException(it) }
    }

    private fun mapException(exception: Throwable): PersistenceException {
        return when (exception) {
            is DataChangedException -> InconsistentDataException(exception)
            is InvalidResultException -> UnexpectedResultException(exception)
            is MappingException -> PersistenceException(exception)
            is DataTypeException -> PersistenceException(exception)
            is DetachedException -> DatabaseConnectionException(exception)
            is DataAccessException -> QueryException(exception)
            else -> PersistenceException(exception)
        }
    }
}