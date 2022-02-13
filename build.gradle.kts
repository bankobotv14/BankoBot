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
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2" // used for plugin-processor
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.4.3"
    application
    antlr
}

group = "de.nycode"
version = "1.3.2"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://schlaubi.jfrog.io/artifactory/lavakord")
    maven("https://schlaubi.jfrog.io/artifactory/forp")
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

application {
    mainClass.set("de.nycode.bankobot.LauncherKt")
}

dependencies {
    // this one is included in the bot itself, therefore we make it compileOnly
    @Suppress("DependencyOnStdlib")
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("dev.schlaubi", "mikbot-api", "2.2.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "1.0.0")

    implementation("com.vladsch.flexmark", "flexmark-html2md-converter", "0.62.2")

    implementation("dev.schlaubi.lavakord", "kord", "2.0.0")

    implementation("org.ow2.asm", "asm", "9.2")
    implementation("org.ow2.asm", "asm-tree", "9.2")

    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.17.1")

    antlr("org.antlr", "antlr4", "4.9.2")

    implementation(project(":autohelp:kord"))
    implementation("dev.schlaubi.forp", "forp-analyze-client", "1.0-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
    testRuntimeOnly("org.slf4j", "slf4j-simple", "1.7.30")
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.6.0")
    testImplementation("com.willowtreeapps.assertk", "assertk", "0.25")
}

// Kotlin dsl
tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
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

detekt {
    autoCorrect = true
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks {
        withType<Detekt>().configureEach {
            // Target version of the generated JVM bytecode. It is used for type resolution.
            this.jvmTarget = "17"

            autoCorrect = true
        }
    }
}

pluginPublishing {
    repositoryUrl.set("https://updates.bankobot.schlaubi.net")
    targetDirectory.set(rootProject.file("ci-repo").toPath())
    projectUrl.set("https://github.com/DRSchlaubi/mikbot/")
}
