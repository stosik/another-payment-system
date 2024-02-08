package pl.stosik.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import pl.stosik.messaging.ApplicationConfiguration.KafkaConfiguration.BootstrapServers
import pl.stosik.messaging.kafka.KafkaConfiguration
import java.nio.file.Files
import java.nio.file.Paths

object IntegrationTestConfig {

    private val containers = AppContainers.apply { start() }

    val objectMapper = KafkaConfiguration.objectMapper

    val applicationConfig = parseConfiguration("src/test/resources/application-test.yaml").let {
        it.copy(
            kafka = it.kafka.copy(
                consumer = it.kafka.consumer.copy(
                    bootstrap = BootstrapServers(AppContainers.bootstrapServers)
                ),
                producer = it.kafka.producer.copy(
                    bootstrap = BootstrapServers(AppContainers.bootstrapServers)
                )
            )
        )
    }
}

fun parseConfiguration(filePath: String): ApplicationConfiguration {
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val path = Paths.get(filePath)

    return Files.newBufferedReader(path)
        .use {
            mapper.readValue(it, ApplicationConfiguration::class.java)
        }
}
