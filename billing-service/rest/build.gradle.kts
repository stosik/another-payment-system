plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlinProject()
ktorServerLibs()
ktorClientLibs()
metricLibs()

dependencies {
    implementation(project(":core"))
    implementation(project(":models"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
