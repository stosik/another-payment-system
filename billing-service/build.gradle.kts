import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
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
                "-Xskip-prerelease-check",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.OptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
