import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
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

/**
 * Configures data layer libs needed for interacting with the DB
 */
fun Project.dataLibs() {
    dependencies {
        "implementation"("org.jooq:jooq:${Versions.JOOQ_VERSION}")
        "implementation"("org.jooq:jooq-meta-extensions:${Versions.JOOQ_VERSION}")
        "implementation"("org.jooq:jooq-meta:${Versions.JOOQ_VERSION}")
        "implementation"("org.jooq:jooq-kotlin-coroutines:${Versions.JOOQ_VERSION}")
        "implementation"("org.jooq:jooq-kotlin:${Versions.JOOQ_VERSION}")

        "implementation"("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
        "implementation"("io.r2dbc:r2dbc-spi:${Versions.R2DBC_VERSION}")
        "implementation"("io.r2dbc:r2dbc-pool:${Versions.R2DBC_VERSION}")
        "implementation"("org.postgresql:postgresql:${Versions.POSTGRESQL_VERSION}")
    }
}