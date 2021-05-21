/*
 *     This file is part of the BankoBot Project.
 *     Copyright (C) 2021  BankoBot Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <https://www.gnu.org/licenses/>.
 *
 */

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("kapt") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    application
    antlr
}

group = "de.nycode"
version = "1.2.0-hotifx.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://kotlin.bintray.com/kotlinx/")
    maven("https://schlaubi.jfrog.io/artifactory/lavakord")
    maven("https://schlaubi.jfrog.io/artifactory/forp")
}

application {
    mainClass.set("de.nycode.bankobot.LauncherKt")
}

dependencies {
    runtimeOnly(kotlin("scripting-jsr223"))
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.1.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.0") {
        version {
            strictly("1.0.0")
        }
    }

    implementation("dev.kord", "kord-core", "kotlin-1.5-SNAPSHOT") {
        version {
            strictly("kotlin-1.5-SNAPSHOT")
        }
    }
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
