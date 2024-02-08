plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":data"))
    implementation("pl.stosik.messaging:kafka-messaging:1.0.0")
    implementation("org.quartz-scheduler:quartz:${Versions.QUARTZ_VERSION}")

    api(project(":models"))
}