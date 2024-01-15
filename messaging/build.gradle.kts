plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.KOTLIN_COROUTINES_VERSION}")
    implementation("org.apache.kafka:kafka-clients:${Versions.KAFKA_VERSION}")
    implementation("io.projectreactor.kafka:reactor-kafka:${Versions.KAFKA_REACTOR_VERSION}")
}