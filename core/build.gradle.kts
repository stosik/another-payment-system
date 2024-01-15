plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":data"))
    implementation(project(":messaging"))
    implementation("org.quartz-scheduler:quartz:${Versions.QUARTZ_VERSION}")

    api(project(":models"))
}