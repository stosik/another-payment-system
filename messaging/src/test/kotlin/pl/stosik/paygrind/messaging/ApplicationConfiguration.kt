package pl.stosik.paygrind.messaging

interface Configuration {

    fun toMap(): Map<String, Any?>
}

data class ApplicationConfiguration(val kafka: KafkaConfiguration) : Configuration {

    override fun toMap() = this.asMap()

    data class KafkaConfiguration(
        val consumer: KafkaConsumerConfiguration,
        val producer: KafkaProducerConfiguration,
    ) : Configuration {
        override fun toMap() = this.asMap()

        data class KafkaConsumerConfiguration(
            val group: GroupId,
            val bootstrap: BootstrapServers,
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
