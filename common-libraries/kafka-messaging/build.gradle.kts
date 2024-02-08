import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
    java
    `java-library`
}

group = "pl.stosik.messaging"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.0")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.21")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")

    // Arrow
    implementation("io.arrow-kt:arrow-core:1.2.1")
    implementation("io.arrow-kt:arrow-resilience-jvm:1.2.1")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // Mockk
    testImplementation("io.mockk:mockk:1.13.8")

    // Kotest
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.7.2")
    testImplementation("io.kotest:kotest-framework-engine:5.7.2")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow-fx-coroutines:1.4.0")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")

    // Kotlin Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testImplementation("org.testcontainers:postgresql:1.19.1")
    testImplementation("org.testcontainers:kafka:1.19.1")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.9"
        jvmTarget = "${JavaVersion.VERSION_17}"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xcontext-receivers",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}