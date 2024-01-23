package pl.stosik.paygrind.data.configuration

import org.jooq.Configuration
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal data class JooqConfigurationElement(
    val value: Configuration,
) : AbstractCoroutineContextElement(JooqConfigurationElement) {
    companion object Key : CoroutineContext.Key<JooqConfigurationElement>
}