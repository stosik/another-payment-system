package pl.stosik.messaging

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicBoolean

object AppContainers {

    val bootstrapServers: List<String>
        get() = kafka.bootstrapServers.split(",")

    private val started = AtomicBoolean(false)

    private val kafka: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.6"))
            .withReuse(true)
    }

    fun start() {
        if (!started.getAndSet(true)) {
            Startables.deepStart(kafka).join()
        }
    }
}