plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

kotlinProject()

dataLibs()
ktorServerLibs()
metricLibs()

application {
    mainClass.set("pl.stosik.billing.app.BillingApp")
}

dependencies {
    implementation("io.arrow-kt:suspendapp:${Versions.SUSPENDAPP_VERSION}")
    implementation("com.zaxxer:HikariCP:${Versions.HIKARI_CP_VERSION}")

    implementation("pl.stosik.messaging:kafka-messaging:1.0.0")
    implementation("org.apache.kafka:kafka-clients:3.6.0")

    implementation(project(":data"))
    implementation(project(":rest"))
    implementation(project(":core"))
    implementation(project(":models"))
}