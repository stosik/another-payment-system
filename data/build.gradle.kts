import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Target

plugins {
    kotlin("jvm")
    id("org.liquibase.gradle") version Versions.LIQUIBASE_GRADLE_VERSION
}

buildscript {
    dependencies {
        classpath("org.jooq:jooq-codegen:${Versions.JOOQ_VERSION}")
        classpath("org.postgresql:postgresql:${Versions.POSTGRESQL_VERSION}")
        classpath("org.jooq:jooq-meta-extensions:${Versions.JOOQ_VERSION}")
    }
}

kotlinProject()
dataLibs()

dependencies {
    api(project(":models"))
    runtimeOnly("org.postgresql:postgresql:${Versions.POSTGRESQL_VERSION}")

    liquibaseRuntime("org.postgresql:postgresql:${Versions.POSTGRESQL_VERSION}")
    liquibaseRuntime("org.liquibase:liquibase-core:${Versions.LIQUIBASE_CORE_VERSION}")
    liquibaseRuntime("liquibaseRuntime", "org.liquibase:liquibase-gradle-plugin:${Versions.LIQUIBASE_GRADLE_VERSION}")
    liquibaseRuntime("info.picocli:picocli:4.7.1")
    liquibaseRuntime("org.yaml:snakeyaml:2.0")
}

tasks {
    val generateJooq by registering {
        doLast {
            generateJooq()
        }
    }
    named("build") {
        dependsOn(":generateJooq")
    }
    named("compileKotlin") {
        dependsOn(":generateJooq")
    }
}

val projectDir = rootProject.projectDir.toString().trimEnd { it == '/' }

liquibase {
    val props = Properties().apply { load(file(".env").inputStream()) }

    data class ActivityOptions(
        val name: String,
        val arguments: Map<String, String>,
    )

    val activities = listOf(
        ActivityOptions(
            name = "main",
            arguments = mapOf(
                "logLevel" to "info",
                "changeLogFile" to "src/main/resources/db/changelog.sql",
                "url" to props.getProperty("DATABASE_DSN"),
                "username" to props.getProperty("MIGRATION_USER"),
                "password" to props.getProperty("MIGRATION_PASSWORD"),
                "liquibaseSchemaName" to props.getProperty("DATABASE_SCHEMA"),
                "defaultSchemaName" to props.getProperty("DATABASE_SCHEMA"),
                "preserveSchemaCase" to "true",
            ),
        ),
    )
    activities {
        activities.forEach {
            register(it.name) { this.arguments = it.arguments }
        }
    }
    runList = project.ext.properties["runList"]
}

val jooqGenerationTargetDir = "$projectDir/build/generated/sources/jooq"

sourceSets["main"].java.srcDir(jooqGenerationTargetDir)

fun generateJooq() {
    val conf =  Configuration().apply {
        generator =  org.jooq.meta.jaxb.Generator().apply {
            database = Database().apply {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties = listOf(
                    Property()
                        .withKey("scripts")
                        .withValue("$projectDir/src/main/resources/db/changelog.sql"),
                    Property()
                        .withKey("sort")
                        .withValue("alphanumeric"),
                    Property()
                        .withKey("defaultNameCase")
                        .withValue("lower"),
                )
            }
            generate = Generate().apply {
                isDeprecationOnUnknownTypes = false
                isJavaTimeTypes = true
                isKotlinNotNullPojoAttributes = true
                isKotlinNotNullRecordAttributes = true
                isKotlinNotNullInterfaceAttributes = true
            }
            target =  Target().apply {
                packageName = "database.schema"
                directory = jooqGenerationTargetDir
            }
        }
    }

    GenerationTool.generate(conf)
}