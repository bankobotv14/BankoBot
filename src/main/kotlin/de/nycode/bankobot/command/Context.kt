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

package de.nycode.bankobot.command

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.x.commands.kord.model.KordEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
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
        embeds.add(base.apply { builder() })
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
            embeds = mutableListOf(base.apply { builder() })
        }

    suspend fun delete()

    suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage
}

fun KordCommandEvent.toContext(): Context = DelegatedKordCommandContext(this)

internal class DelegatedKordCommandContext(private val delegate: KordCommandEvent) : Context, KordEvent by delegate {
    override val command: Command<*> get() = delegate.command
    override val commands: Map<String, Command<*>> get() = delegate.commands
    override val processor: CommandProcessor get() = delegate.processor

    override suspend fun createResponse(builder: suspend MessageCreateBuilder.() -> Unit): EditableMessage {
        val message = delegate.message.channel.createMessage {
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
