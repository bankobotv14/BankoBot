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
