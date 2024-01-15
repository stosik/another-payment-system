plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":core"))
    implementation(project(":models"))

    implementation("io.javalin:javalin:${Versions.JAVALIN_VERSION}")
    testImplementation("io.javalin:javalin-testtools:${Versions.JAVALIN_VERSION}")
}
