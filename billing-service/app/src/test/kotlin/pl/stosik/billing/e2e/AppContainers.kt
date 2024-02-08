package pl.stosik.billing.e2e

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicBoolean

object AppContainers {

    val jdbcUrl: String
        get() = postgres.jdbcUrl

    val dbUsername: String
        get() = postgres.username

    val dbPassword: String
        get() = postgres.password

    val bootstrapServers: List<String>
        get() = kafka.bootstrapServers.split(",")

    private val started = AtomicBoolean(false)

    private val postgres: PostgreSQLContainer<Nothing> by lazy {
        PostgreSQLContainer<Nothing>("postgres:15.2")
    }

    private val kafka: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.6"))
            .withReuse(true)
    }

    fun start() {
        if (!started.getAndSet(true)) {
            Startables.deepStart(postgres, kafka).join()
        }
    }
}