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

package me.schlaubi.autohelp.kord

import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.addReaction
import dev.schlaubi.forp.fetch.input.FileInput
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import me.schlaubi.autohelp.source.EventSource
import me.schlaubi.autohelp.source.ReceivedMessage

/**
 * Implementation of [EventSource] using [MessageCreateEvent].
 *
 * @param kord the [Kord] instance providing the events
 */
public class KordEventSource(kord: Kord) : EventSource<KordReceivedMessage> {
    override val events: Flow<KordReceivedMessage> = kord.events
        .filterIsInstance<MessageCreateEvent>()
        .map { KordReceivedMessage(it.message) }
}

/**
 * Implementation of [ReceivedMessage] using [kordMessage].
 */
public class KordReceivedMessage(public val kordMessage: Message) : ReceivedMessage {
    override val channelId: Long
        get() = kordMessage.channelId.value
    override val authorId: Long?
        get() = kordMessage.author?.id?.value
    override val guildId: Long
        get() = kordMessage.data.guildId.value!!.value
    override val content: String
        get() = kordMessage.content
    override val files: List<ReceivedMessage.ReceivedFile>
        get() = kordMessage.attachments.map {
            val type = if (it.isImage) {
                FileInput.FileType.IMAGE
            } else {
                FileInput.FileType.PLAIN_TEXT
            }

            KordReceivedFile(type, it.proxyUrl, kordMessage.kord.resources.httpClient)
        } + kordMessage.embeds.mapNotNull {
            it.url?.let { url ->
                KordReceivedFile(FileInput.FileType.IMAGE,
                    url,
                    kordMessage.kord.resources.httpClient)
            }
        }

    override suspend fun react(emoji: DiscordEmoji): Unit = kordMessage.addReaction(emoji)

    public class KordReceivedFile(
        override val type: FileInput.FileType,
        private val url: String,
        private val httpClient: HttpClient,
    ) : ReceivedMessage.ReceivedFile {

        override suspend fun download(): ByteReadChannel = httpClient.get(url)
    }
}
