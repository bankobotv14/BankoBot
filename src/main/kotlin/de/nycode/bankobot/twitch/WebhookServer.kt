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

package de.nycode.bankobot.twitch

import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import de.nycode.bankobot.utils.sha256
import dev.kord.core.Kord
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import mu.KotlinLogging

internal val webhookLogger by lazy { KotlinLogging.logger("Webhooks") }

/**
 * Launches a ktor embedded server uses for receiving webhook notifications from the twitch api
 */
internal fun Kord.launchServer() = embeddedServer(CIO) {
    install(Routing) {
        route("twitch") {
            get {
                webhookLogger.debug { "Receiving webhook call with parameters: ${call.parameters}" }
                when {
                    call.parameters.contains("hub.challenge") -> {
                        val challenge = call.parameters["hub.challenge"]

                        if (challenge == null) {
                            webhookLogger.error("Failed Webhook Challenge")
                            call.respond(HttpStatusCode.BadRequest)
                            return@get
                        }

                        webhookLogger.debug { "Receiving Webhook Challenge $challenge" }
                        call.respondText(challenge)
                    }
                    call.parameters.contains("hub.reason") -> {
                        webhookLogger.error { "Could not create subscription: ${call.parameters["hub.reason"]}" }
                        call.respond(HttpStatusCode.OK)
                    }
                    else -> {
                        call.respond(HttpStatusCode.OK, "Lösch dich")
                    }
                }
            }

            post {
                if (Config.ENVIRONMENT == Environment.PRODUCTION) {
                    val signature = call.request.header("X-Hub-Signature")
                    if (Config.WEBHOOK_SECRET.sha256() != signature) {
                        webhookLogger.warn("Unable to verify signature of request!")
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                    webhookLogger.info("Successfully verified signature of request!")
                }
                call.respond(HttpStatusCode.OK)

                val stream = call.receive<TwitchStreamsResponse>()
                    .data
                    .firstOrNull()
                    ?: TwitchStream()
                updatePresence(stream)
            }
        }
    }
    install(ContentNegotiation) {
        json()
    }
}.start()
