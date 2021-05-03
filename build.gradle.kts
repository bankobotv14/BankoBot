/*
 * MIT License
 *
 * Copyright (c) 2021 BankoBot Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    application
    antlr
}

group = "de.nycode"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://kotlin.bintray.com/kotlinx/")
    maven("https://schlaubi.jfrog.io/artifactory/lavakord")
    maven("https://schlaubi.jfrog.io/artifactory/forp")

    jcenter()
}

application {
    mainClass.set("de.nycode.bankobot.LauncherKt")
}

dependencies {
    runtimeOnly(kotlin("scripting-jsr223"))
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.1.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.1")

    implementation("dev.kord", "kord-core", "0.7.0-RC3")
    implementation("dev.kord.x", "emoji", "0.5.0-SNAPSHOT")
    implementation("dev.kord.x", "commands-runtime-kord", "0.4.0-SNAPSHOT")
    kapt("dev.kord.x", "commands-processor", "0.4.0-SNAPSHOT")

    val ktorVersion = "1.4.1"
    implementation("io.ktor", "ktor-client", ktorVersion)
    implementation("io.ktor", "ktor-client-cio", ktorVersion)
    implementation("io.ktor", "ktor-client-json", ktorVersion)
    implementation("io.ktor", "ktor-serialization", ktorVersion)

    implementation("io.ktor", "ktor-server-core", ktorVersion)
    implementation("io.ktor", "ktor-server-cio", ktorVersion)

    implementation("io.github.microutils", "kotlin-logging", "1.12.0")
    implementation("io.github.cdimascio", "dotenv-kotlin", "6.2.2")

    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.2.3")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.sentry", "sentry", "3.1.0")
    implementation("io.sentry", "sentry-logback", "3.2.0")

    implementation("com.vladsch.flexmark", "flexmark-html2md-converter", "0.60.2")

    implementation("dev.schlaubi.lavakord", "kord", "1.0.0-SNAPSHOT")

    implementation("org.ow2.asm", "asm", "9.1")
    implementation("org.ow2.asm", "asm-tree", "9.1")

    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.15.0")

    antlr("org.antlr", "antlr4", "4.9.1")

    implementation(project(":autohelp:kord"))
    implementation("dev.schlaubi.forp", "forp-analyze-client", "1.0-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.6.0")
    testRuntimeOnly("org.slf4j", "slf4j-simple", "1.7.30")
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.4.2")
    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.23")
}

// Kotlin dsl
tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    withType<Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = "1.8"
    }

    generateGrammarSource {
        outputDirectory =
            File("${project.buildDir}/generated-src/antlr/main/de/nycode/bankobot/variables")
        arguments = arguments + listOf("-visitor", "-package", "de.nycode.bankobot.variables")
    }

    test {
        useJUnitPlatform()
    }
}
