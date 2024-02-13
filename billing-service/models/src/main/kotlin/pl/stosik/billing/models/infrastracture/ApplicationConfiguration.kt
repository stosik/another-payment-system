package pl.stosik.billing.models.infrastracture

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

interface Configuration {
    fun toMap(): Map<String, Any?>
}

data class ApplicationConfiguration(
    val cron: CronConfiguration,
    val database: DatabaseConfiguration,
    val kafka: KafkaConfiguration,
) : Configuration {

    override fun toMap() = this.asMap()

    data class CronConfiguration(
        val instanceId: String,
        val expression: String? = null
    )

    data class DatabaseConfiguration(
        val url: String,
        val username: String,
        val password: String,
    ) : Configuration {
        override fun toMap() = this.asMap()
    }

    data class KafkaConfiguration(
        val consumer: KafkaConsumerConfiguration,
        val producer: KafkaProducerConfiguration,
    ) : Configuration {
        override fun toMap() = this.asMap()

        data class KafkaConsumerConfiguration(
            val bootstrap: BootstrapServers,
            val group: GroupId,
            val key: KeyDeserializer,
            val auto: AutoOffset,
            val enable: AutoCommit
        ) : Configuration {
            override fun toMap() = this.asMap()

            data class GroupId(val id: String)

            data class KeyDeserializer(val deserializer: String)

            data class AutoOffset(val offset: Offset) {
                data class Offset(val reset: String)
            }

            data class AutoCommit(val auto: Commit) {
                data class Commit(val commit: Boolean)
            }
        }

        data class KafkaProducerConfiguration(
            val bootstrap: BootstrapServers,
            val key: KeySerializer
        ) : Configuration {

            override fun toMap() = this.asMap()

            data class KeySerializer(val serializer: String)
        }

        data class BootstrapServers(val servers: List<String>)
    }
}

/**
 * Creates a map structure based on the class properties
 * data class Foo(val bar: String, val baz: Int) is transformed
 * to mapOf("bar" to "bar", "baz" to 1)
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
 * Example:
 * mapOf("foo" to mapOf("bar" to "baz")) with delimiter "." is transformed to mapOf("foo.bar" to "baz")
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