package pl.stosik.paygrind.messaging.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import pl.stosik.paygrind.messaging.IntegrationTestConfig.objectMapper
import pl.stosik.paygrind.messaging.KafkaTestUtils
import pl.stosik.paygrind.messaging.KafkaTestUtils.TEST_TOPIC
import pl.stosik.paygrind.messaging.KafkaTestUtils.emitEvents
import java.util.*

internal class EventConsumerTest {

    @Test
    @Disabled
    fun `refund authorized event is consumed successfully`(): Unit = runTest {
        val id = UUID.randomUUID().toString()
        val testEvent = TestEvent(id, "test")

        emitEvents(TEST_TOPIC, testEvent)

        val record = fetchTestEvent(id)
        record shouldBeEqual testEvent
    }

    private suspend fun fetchTestEvent(id: String): TestEvent {
        return KafkaTestUtils.createTestReceiver(TEST_TOPIC)
            .receive()
            .asFlow()
            .map {
                it.receiverOffset().commit().awaitFirstOrNull()
                objectMapper.readValue<TestEvent>(it.value()) to it
            }
            .filter { (event, _) -> event.id == id }
            .first()
            .first
    }
}

data class TestEvent(val id: String, val name: String)
