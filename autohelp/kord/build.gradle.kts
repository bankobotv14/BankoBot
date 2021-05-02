plugins {
    kotlin("jvm")
}

group = "me.schlaubi.autohelp"
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/forp")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":autohelp"))
    api("dev.kord", "kord-core", "0.7.0-RC3")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
}

kotlin {
    explicitApi()
}

apply(from = "../publishing.gradle.kts")
