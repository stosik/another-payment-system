package pl.stosik.messaging

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Creates a map structure based on the class properties
 */
fun <T : Any> T.asMap(): Map<String, Any?> {
    return (this::class as KClass<T>).memberProperties.associate { prop ->
        prop.name to prop.get(this)?.let { value ->
            if (value::class.isData) {
                value.asMap()
            } else {
                value
            }
        }
    }
}

/**
 * Creates a flat map structure that joins nested keys with given delimiter, yielding non-nested result.
 */
@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.joinKeysFlattening(
    prefix: String = "",
    delimiter: String = ".",
): Map<String, Any> {
    return this.entries
        .filter { it.value != null }
        .map { it.key to it.value!! }
        .flatMap { (key, value) ->
            val newKey = if (prefix.isEmpty()) key else "$prefix$delimiter$key"
            when (value) {
                is Map<*, *> ->
                    (value.filterKeys { it is String } as Map<String, Any>)
                        .joinKeysFlattening(newKey, delimiter)
                        .map { it.key to it.value }

                else -> listOf(newKey to value)
            }
        }
        .toMap()
}