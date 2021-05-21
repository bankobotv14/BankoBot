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
