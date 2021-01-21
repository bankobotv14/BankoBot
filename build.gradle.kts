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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    application
}

group = "de.nycode"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://kotlin.bintray.com/kotlinx/")
}

application {
    mainClass.set("de.nycode.bankobot.LauncherKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")


    implementation("dev.kord", "kord-core", "0.7.0-SNAPSHOT")
    implementation("dev.kord.x:commands-runtime-kord:0.4.0-SNAPSHOT")
    kapt("dev.kord.x:commands-processor:0.4.0-SNAPSHOT")

    implementation("io.ktor:ktor-client:1.4.3")
    implementation("io.ktor:ktor-client-cio:1.4.3")
    implementation("io.ktor:ktor-client-json:1.4.3")
    implementation("io.ktor:ktor-serialization:1.4.3")

    implementation("io.github.microutils", "kotlin-logging", "1.12.0")
    implementation("io.github.cdimascio", "dotenv-kotlin", "6.2.2")

    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.2.3")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.sentry", "sentry", "3.1.0")
    implementation("io.sentry", "sentry-logback", "3.2.0")

    implementation("com.vladsch.flexmark", "flexmark-html2md-converter", "0.60.2")


    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.15.0")
}


// Kotlin dsl
tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    withType<Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = "1.8"
    }
}
