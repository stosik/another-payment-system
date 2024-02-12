import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

fun Project.kotlinProject() {
    dependencies {
        // Kotlin libs
        "implementation"(kotlin("stdlib"))
        "implementation"(kotlin("reflect"))

        // Kotlin Coroutines
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES_VERSION}")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.KOTLIN_COROUTINES_VERSION}")

        // Logging
        "implementation"("org.slf4j:slf4j-simple:2.0.9")
        "implementation"("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING_VERSION}")

        // Arrow
        "implementation"("io.arrow-kt:arrow-core:${Versions.ARROW_VERSION}")
        "implementation"("io.arrow-kt:arrow-resilience-jvm:${Versions.ARROW_VERSION}")
        "implementation"("io.arrow-kt:arrow-fx-coroutines:${Versions.ARROW_VERSION}")

        // Jackson
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JACKSON_VERSION}")
        "implementation"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.JACKSON_VERSION}")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.JACKSON_VERSION}")

        // Kotlin Coroutines Test
        "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLIN_COROUTINES_VERSION}")
        "testImplementation"("org.awaitility:awaitility-kotlin:${Versions.AWAITILITY_VERSION}")

        // Mockk
        "testImplementation"("io.mockk:mockk:${Versions.MOCKK_VERSION}")

        // Kotest
        "testImplementation"("io.kotest:kotest-assertions-core-jvm:${Versions.KOTEST_VERSION}")
        "testImplementation"("io.kotest:kotest-framework-engine:${Versions.KOTEST_VERSION}")
        "testImplementation"("io.kotest.extensions:kotest-assertions-arrow:${Versions.KOTEST_ARROW}")
        "testImplementation"("io.kotest.extensions:kotest-assertions-arrow-fx-coroutines:${Versions.KOTEST_ARROW}")
        "testImplementation"("io.kotest.extensions:kotest-extensions-testcontainers:${Versions.KOTEST_TEST_CONTAINERS}")

        // JUnit 5
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_VERSION}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT_VERSION}")
        "runtimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_VERSION}")

        // Testcontainers
        "testImplementation"("org.testcontainers:testcontainers:${Versions.TEST_CONTAINERS_VERSION}")
        "testImplementation"("org.testcontainers:junit-jupiter:${Versions.TEST_CONTAINERS_VERSION}")
        "testImplementation"("org.testcontainers:postgresql:${Versions.TEST_CONTAINERS_VERSION}")
        "testImplementation"("org.testcontainers:kafka:${Versions.TEST_CONTAINERS_VERSION}")
    }
}

fun Project.dataLibs() {
    dependencies {
        "implementation"("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED_VERSION}")
        "implementation"("org.jetbrains.exposed:exposed-dao:${Versions.EXPOSED_VERSION}")
        "implementation"("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED_VERSION}")
        "implementation"("org.xerial:sqlite-jdbc:3.30.1")
    }
}

fun Project.ktorServerLibs() {
    dependencies {
        "implementation"("io.arrow-kt:suspendapp-ktor:${Versions.SUSPENDAPP_VERSION}")
        "implementation"("io.ktor:ktor-server-content-negotiation:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-server-core:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-server-default-headers:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-server-netty:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-server-metrics-micrometer:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-server-resources:${Versions.KTOR_VERSION}")

        "testImplementation"("io.ktor:ktor-server-tests:${Versions.KTOR_VERSION}")
    }
}

fun Project.ktorClientLibs() {
    dependencies {
        "implementation"("io.ktor:ktor-client-core:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-client-cio:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-client-serialization:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-client-content-negotiation:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-client-resources:${Versions.KTOR_VERSION}")
        "implementation"("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR_VERSION}")
    }
}

fun Project.metricLibs() {
    dependencies {
        "implementation"("com.sksamuel.cohort:cohort-core:${Versions.COHORT_VERSION}")
        "implementation"("com.sksamuel.cohort:cohort-ktor2:${Versions.COHORT_VERSION}")
        "implementation"("com.sksamuel.cohort:cohort-hikari:${Versions.COHORT_VERSION}")
        "implementation"("com.sksamuel.cohort:cohort-kafka:${Versions.COHORT_VERSION}")
        "implementation"("io.micrometer:micrometer-registry-prometheus:${Versions.PROMETHEUS_VERSION}")
    }
}