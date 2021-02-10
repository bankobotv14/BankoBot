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
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Subscribes to or Unsubscribes, depending on the [mode], from an Twitch API Webhook Subscription.
 * @param userId the user to subscribe or unsubscribe from
 * @param mode either "subscribe" or "unsubscribe"
 * @param token the twitch app access token to use
 * @param duration the duration to submit to twitch
 */
@Suppress("MagicNumber")
internal suspend fun HttpClient.updateSubscription(
    userId: Int,
    mode: String,
    token: TwitchAccessTokenResponse,
    duration: Int = 86400
) =
    post<HttpResponse>("https://api.twitch.tv/helix/webhooks/hub") {
        body = TwitchSubscriptionRequest {
            callback = Config.WEBHOOK_URL
            this.mode = mode
            leaseSeconds = duration
            topic = "https://api.twitch.tv/helix/streams?user_id=${userId}"
            if (Config.ENVIRONMENT == Environment.PRODUCTION) {
                secret = Config.WEBHOOK_SECRET
            }
        }
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        header("Client-ID", Config.TWITCH_CLIENT_ID)
    }
