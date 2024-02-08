import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.9.22" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

allprojects {
    group = "pl.stosik"
    version = "1.0"

    repositories {
        mavenCentral()
        mavenLocal()
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

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
