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

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.EmbedBuilder
import me.schlaubi.autohelp.help.EditableMessage
import me.schlaubi.autohelp.help.Embed
import me.schlaubi.autohelp.help.MessageRenderer
import me.schlaubi.autohelp.help.OutgoingMessage

internal class KordMessageRenderer(private val kord: Kord) : MessageRenderer {
    override suspend fun sendMessage(guildId: Long, channelId: Long, message: OutgoingMessage): EditableMessage {
        val kordMessage = (kord.getGuild(Snowflake(guildId))
            ?.getChannelOrNull(Snowflake(channelId)) as MessageChannelBehavior).createMessage {
            content = message.content
            message.embed?.let { embed(it.toBuilder()) }
        }

        return KordEditableMessage(kordMessage)
    }
}

private class KordEditableMessage(private val kordMessage: MessageBehavior): EditableMessage {
    override suspend fun edit(message: OutgoingMessage) {
        kordMessage.edit {
            content = message.content
            message.embed?.let { embed(it.toBuilder()) }
        }
    }
}

private fun Embed.toBuilder(): EmbedBuilder.() -> Unit = {
    title = this@toBuilder.title
    description = this@toBuilder.description
    val footer = this@toBuilder.footer
    this@toBuilder.fields.forEach {
        field {
            name = it.title
            value = it.value
            inline = it.inline
        }
    }
    if (footer != null) {
        footer {
            text = footer.name
            icon = footer.avatar
        }
    }
}
