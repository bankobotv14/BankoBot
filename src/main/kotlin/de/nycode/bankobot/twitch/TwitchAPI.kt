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
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TwitchUser(
    val id: Int,
    @SerialName("login")
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val type: String,
    @SerialName("broadcaster_type")
    val broadcasterType: String,
    val description: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String,
    @SerialName("offline_image_url")
    val offlineImageUrl: String,
    @SerialName("view_count")
    val viewCount: Int
)

@Serializable
data class TwitchSubscriptionRequest(
    @SerialName("hub.callback")
    var callback: String = "",
    @SerialName("hub.mode")
    var mode: String = "",
    @SerialName("hub.topic")
    var topic: String = "",
    @SerialName("hub.lease_seconds")
    var leaseSeconds: Int = 0,
    @SerialName("hub.secret")
    var secret: String? = null,
    @Transient
    val configuration: TwitchSubscriptionRequest.() -> Unit = {}
) {
    init {
        apply(configuration)
    }
}

internal suspend fun TwitchUser.fetchStream(token: TwitchAccessTokenResponse): TwitchStream {
    return BankoBot.httpClient.get<TwitchStreamsResponse>("https://api.twitch.tv/helix/streams") {
        header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        header("Client-ID", Config.TWITCH_CLIENT_ID)
        parameter("user_id", id)
    }
        .data
        .firstOrNull() ?: TwitchStream()
}

internal suspend fun fetchUser(token: TwitchAccessTokenResponse): TwitchUser? {
    return try {
        BankoBot.httpClient.get<TwitchUserResponse>("https://api.twitch.tv/helix/users") {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            header("Client-ID", Config.TWITCH_CLIENT_ID)
            parameter("login", Config.TWITCH_CHANNEL)
        }.data.firstOrNull()
    } catch (ignored: ClientRequestException) {
        ignored.printStackTrace()
        null
    }
}
