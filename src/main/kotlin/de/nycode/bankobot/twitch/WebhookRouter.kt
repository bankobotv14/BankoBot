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

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import de.nycode.bankobot.utils.sha256
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
import mu.KotlinLogging

internal val webhookLogger by lazy { KotlinLogging.logger("Webhooks") }

/**
 * Routes for receiving webhook notifications from the twitch api
 */
@OptIn(KtorExperimentalAPI::class)
internal fun Route.twitch() {
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
        BankoBot.kord.updatePresence(stream)
    }
}
