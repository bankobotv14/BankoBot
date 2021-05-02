import java.util.*

apply(plugin = "org.gradle.maven-publish")
apply(plugin = "org.gradle.signing")

val cmp = components

val configurePublishing: PublishingExtension.() -> Unit = {
    repositories {
        maven {
            setUrl("https://schlaubi.jfrog.io/artifactory/forp-artifacts")

            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_KEY")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(cmp["java"])

            groupId = group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(project.name)
                description.set(
                    """
                    Autohelp is a Kotlin Library for Discord bots which want to automatically send dynamic help messages whenever someone
                    sends something providing a JVM Stacktrace.
                """.trimIndent()
                )
                url.set("https://github.com/DRSchlaubi/furry-okto-rotary-phone")

                licenses {
                    license {
                        name.set("Apache-2.0 License")
                        url.set("https://github.com/DRSchlaubi/furry-okto-rotary-phone/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        name.set("Michael Rittmeister")
                        email.set("mail@schlaubi.me")
                        organizationUrl.set("https://michael.rittmeister.in")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/bankobotv14/bankbot.git")
                    developerConnection.set("scm:git:https://github.com/bankobotv14/bankbot.git")
                    url.set("https://github.com/bankobotv14/bankbot")
                }
            }

            artifact(tasks["kotlinSourcesJar"])
        }
    }
}

val configureSigning: SigningExtension.() -> Unit = {
    val signingKey = findProperty("signingKey")?.toString()
    val signingPassword = findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(
            String(Base64.getDecoder().decode(signingKey.toByteArray())),
            signingPassword
        )
    }

    publishing.publications.withType<MavenPublication> {
        sign(this)
    }
}

extensions.configure("signing", configureSigning)
extensions.configure("publishing", configurePublishing)

val Project.publishing: PublishingExtension
    get() =
        (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension
