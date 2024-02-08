package pl.stosik.messaging.kafka.base.json

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serializer

class KafkaJacksonSerializer<T>(private val objectMapper: ObjectMapper) : Serializer<T> {

    override fun serialize(topic: String?, data: T): ByteArray? {
        return data?.let { objectMapper.writeValueAsBytes(it) }
    }
}