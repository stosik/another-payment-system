plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

kotlinProject()

dataLibs()

application {
    mainClass.set("pl.stosik.paygrind.app.PaygrindApp")
}

dependencies {
    implementation("io.javalin:javalin:${Versions.JAVALIN_VERSION}")
    implementation("io.arrow-kt:suspendapp:0.4.0")

    implementation(project(":data"))
    implementation(project(":rest"))
    implementation(project(":core"))
    implementation(project(":models"))
    implementation(project(":messaging"))
}