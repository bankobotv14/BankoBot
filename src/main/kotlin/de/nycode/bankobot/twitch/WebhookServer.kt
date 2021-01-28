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
import io.ktor.util.*

/**
 * Launches a ktor embedded server uses for receiving webhook notifications from the twitch api
 */
@OptIn(KtorExperimentalAPI::class)
internal fun Kord.launchServer() = embeddedServer(CIO) {
    install(Routing) {

        get {
            when {
                call.parameters.contains("hub.challenge") -> {
                    call.respondText(call.parameters["hub.challenge"] ?: "lösch dich")
                }
                call.parameters.contains("hub.reason") -> {
                    println("Could not create subscription: ${call.parameters["hub.reason"]}")
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
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }

            val stream = call.receive<TwitchStreamsResponse>()
                .data
                .firstOrNull()
                ?: TwitchStream()
            updatePresence(stream)
            call.respond(HttpStatusCode.OK)
        }
    }
    install(ContentNegotiation) {
        json()
    }
}.start()
