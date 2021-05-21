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

package de.nycode.bankobot.command

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.rest.builder.message.MessageModifyBuilder
import dev.kord.x.commands.kord.model.KordEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.kord.model.respond
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.command.CommandEvent
import dev.kord.x.commands.model.processor.CommandProcessor
import dev.kord.x.emoji.DiscordEmoji

interface Context : CommandEvent, KordEvent {
    suspend fun sendResponse(content: String, builder: MessageCreateBuilder.() -> Unit = {}): EditableMessage =
        createResponse {
            this.content = content
            builder()
        }

    suspend fun sendResponse(emoji: DiscordEmoji): EditableMessage = sendResponse(emoji.unicode)

    suspend fun sendResponse(
        base: EmbedBuilder = EmbedBuilder(),
        builder: suspend EmbedBuilder.() -> Unit = {}
    ): EditableMessage = createResponse {
        embed = base.apply { builder() }
    }

    suspend fun createResponse(builder: suspend MessageCreateBuilder.() -> Unit): EditableMessage

    @Deprecated("Use sendResponse instead", ReplaceWith("sendResponse(emoji)"))
    override suspend fun respond(emoji: DiscordEmoji): Message {
        return super.respond(emoji)
    }

    @Deprecated("Use sendResponse instead", ReplaceWith("sendResponse(message)"))
    override suspend fun respond(message: String): Message {
        return super.respond(message)
    }
}

interface EditableMessage {
    suspend fun edit(content: String): EditableMessage = modify { this.content = content }
    suspend fun edit(emoji: DiscordEmoji): EditableMessage = edit(emoji.unicode)
    suspend fun editEmbed(
        base: EmbedBuilder = EmbedBuilder(),
        builder: suspend EmbedBuilder.() -> Unit = {}
    ): EditableMessage =
        modify {
            embed = base.apply { builder() }
        }

    suspend fun delete()

    suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage
}

fun KordCommandEvent.toContext(): Context = DelegatedKordCommandContext(this)

private class DelegatedKordCommandContext(private val delegate: KordCommandEvent) : Context, KordEvent by delegate {
    override val command: Command<*> get() = delegate.command
    override val commands: Map<String, Command<*>> get() = delegate.commands
    override val processor: CommandProcessor get() = delegate.processor

    override suspend fun createResponse(builder: suspend MessageCreateBuilder.() -> Unit): EditableMessage {
        val message = delegate.respond {
            builder()
        }

        return MessageEditableMessage(message)
    }

    override suspend fun respond(emoji: DiscordEmoji): Message {
        return delegate.respond(emoji)
    }

    override suspend fun respond(message: String): Message {
        return delegate.respond(message)
    }
}

internal class MessageEditableMessage(private val messageBehavior: MessageBehavior) : EditableMessage {
    override suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage {
        val message = messageBehavior.edit {
            builder()
        }

        return MessageEditableMessage(message)
    }

    override suspend fun delete() = messageBehavior.delete()
}
