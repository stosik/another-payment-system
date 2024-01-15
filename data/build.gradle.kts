plugins {
    kotlin("jvm")
}

kotlinProject()

dataLibs()

dependencies {
    api(project(":models"))
    implementation("com.zaxxer:HikariCP:${Versions.HIKARI_CP_VERSION}")
    runtimeOnly("org.postgresql:postgresql:${Versions.POSTGRESQL_VERSION}")
}
