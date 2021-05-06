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
import dev.kord.core.Kord
import dev.kord.rest.request.errorString
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlin.time.ExperimentalTime
import kotlin.time.days
import kotlin.time.milliseconds
import kotlin.time.seconds

const val TOKEN_URL = "https://id.twitch.tv/oauth2/token"

const val CLIENT_ID = "client_id"
const val CLIENT_SECRET = "client_secret"
const val GRANT_TYPE = "grant_type"
const val CLIENT_CREDENTIALS = "client_credentials"

/**
 * Initializes the twitch integration for the bot
 */
internal fun Kord.twitchIntegration() = this.launch {
    val token = fetchAccessToken()

    val user = fetchUser(token) ?: error("Unable to fetch user from twitch!")
    val stream = user.fetchStream(token)
    updatePresence(stream)
    subscribe(user.id, token)
}

/**
 * Updates the bot presence based on a [TwitchStream] object
 * @param stream the stream object received from the twitch api
 */
internal suspend fun Kord.updatePresence(stream: TwitchStream) {
    if (stream.isLive) {
        editPresence {
            streaming(if (stream.title.isBlank()) "Banko auf Twitch" else stream.title, "https://twitch.tv/DerBanko")
        }
    } else {
        editPresence {
            watching("/help")
        }
    }
}

/**
 * Registers a webhook subscription with the twitch api
 * The webhook gets fired when the state of a stream changes
 * @param userId the user to receive a notification for
 * @param token the app authentication token
 */
private suspend fun Kord.subscribe(userId: Int, token: TwitchAccessTokenResponse) = coroutineScope {
    launchServer()
    val response = BankoBot.httpClient.updateSubscription(userId, "subscribe", token)

    registerSubscriptionShutdownHook(userId, token)

    if (response.status == HttpStatusCode.Accepted) {
        webhookLogger.info("Registered subscription for twitch webhook!")
    } else {
        error(response.errorString())
    }
    launchSubscriptionUpdater(userId, token)
}

/**
 * Launches a ticker which updates the subscription every 24 hours.
 * @param userId the user to receive a notification for
 * @param token the app authentication token
 */
@OptIn(ObsoleteCoroutinesApi::class, ExperimentalTime::class)
@Suppress("MagicNumber")
private fun CoroutineScope.launchSubscriptionUpdater(
    userId: Int,
    token: TwitchAccessTokenResponse
) = launch {
    val duration = if (Config.ENVIRONMENT == Environment.PRODUCTION) {
        1.days
    } else {
        30.seconds
    }
    val delay = duration.inWholeMilliseconds
    for (unit in ticker(delayMillis = delay, initialDelayMillis = delay)) {
        BankoBot.httpClient.updateSubscription(
            userId,
            "subscribe",
            token,
            duration = delay.milliseconds.inSeconds.toInt()
        )
        webhookLogger.info("Updated twitch webhook subscription!")
    }
}

/**
 * Registers a shutdown hook to unsubscribe from twitch
 * @param userId the user id to unsubscribe from
 * @param token the app authentication token
 */
private fun registerSubscriptionShutdownHook(
    userId: Int,
    token: TwitchAccessTokenResponse
) {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run(): Unit = runBlocking {
            BankoBot.httpClient.updateSubscription(userId, "unsubscribe", token)
        }
    })
}
