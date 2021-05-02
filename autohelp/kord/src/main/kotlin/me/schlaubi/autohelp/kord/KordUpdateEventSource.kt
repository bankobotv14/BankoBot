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
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.addReaction
import dev.schlaubi.forp.fetch.input.FileInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import me.schlaubi.autohelp.source.EventSource
import me.schlaubi.autohelp.source.ReceivedMessage

public class KordUpdateEventSource(kord: Kord) : EventSource<KordUpdateMessage> {
    override val events: Flow<KordUpdateMessage> = kord.events
        .filterIsInstance<MessageUpdateEvent>()
        .map { KordUpdateMessage(it.message.asMessage()) }
}

public class KordUpdateMessage(public val kordMessage: Message) : ReceivedMessage {
    override val guildId: Long
        get() = kordMessage.data.guildId.value!!.value
    override val channelId: Long
        get() = kordMessage.data.channelId.value
    override val authorId: Long
        get() = kordMessage.author?.id?.value!!
    override val content: String? = null
    override val files: List<ReceivedMessage.ReceivedFile>
        get() = kordMessage.embeds.mapNotNull {
            it.url?.let { url ->
                KordReceivedMessage.KordReceivedFile(FileInput.FileType.IMAGE,
                    url,
                    kordMessage.kord.resources.httpClient)
            }
        }

    override suspend fun react(emoji: DiscordEmoji): Unit = kordMessage.addReaction(emoji)
}