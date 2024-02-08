plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

kotlinProject()

dataLibs()

application {
    mainClass.set("pl.stosik.billing.app.BillingApp")
}

dependencies {
    implementation("io.javalin:javalin:${Versions.JAVALIN_VERSION}")
    implementation("io.arrow-kt:suspendapp:0.4.0")
    implementation("pl.stosik.messaging:kafka-messaging:1.0.0")

    implementation(project(":data"))
    implementation(project(":rest"))
    implementation(project(":core"))
    implementation(project(":models"))
}