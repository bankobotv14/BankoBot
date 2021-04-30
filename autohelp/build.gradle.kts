plugins {
    kotlin("jvm")
}

group = "me.schlaubi"
version = "2.0.0-RC.1"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/forp")
}

dependencies {
    implementation(platform("dev.schlaubi.forp:forp-bom:1.0-SNAPSHOT"))
    implementation("dev.schlaubi.forp", "forp-analyze-api")
    implementation("dev.schlaubi.forp", "forp-parser-api")
}

kotlin {
    explicitApi()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "15"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}
