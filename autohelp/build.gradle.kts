import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm")
}

group = "me.schlaubi"
version = "2.0.0-RC.1"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/forp")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api("dev.schlaubi.forp", "forp-analyze-api-jvm", "1.0-SNAPSHOT")
    api("dev.kord.x", "emoji", "0.5.0-SNAPSHOT")
    implementation("io.github.microutils", "kotlin-logging", "1.12.0")
}

kotlin {
    explicitApi()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn" +
                    "-Xopt-in=kotlin.time.ExperimentalTime"
        }
    }

    processResources {
        from(sourceSets["main"].resources) {
            val hash = ByteArrayOutputStream().use { out ->
                exec {
                    commandLine("git", "rev-parse", "--short", "HEAD")
                    standardOutput = out
                }

                out.toString().trim()
            }
            val tokens = mapOf(
                "version" to version,
                "commit_hash" to hash
            )
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            filter(org.apache.tools.ant.filters.ReplaceTokens::class, mapOf("tokens" to tokens))
        }
    }
}
