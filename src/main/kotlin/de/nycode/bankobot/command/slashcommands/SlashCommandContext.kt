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
@file:OptIn(KordPreview::class)

package de.nycode.bankobot.command.slashcommands

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.EditableMessage
import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.rest.builder.message.MessageModifyBuilder
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.processor.CommandProcessor

class EphemeralSlashCommandContext(
    override val event: MessageCreateEvent,
    private val _command: Command<*>?,
    override val commands: Map<String, Command<*>>,
    override val processor: CommandProcessor,
    private val ack: EphemeralInteractionResponseBehavior
) : Context {

    override val command: Command<*>
        get() = _command ?: error("Command is not available")

    @OptIn(KordUnsafe::class)
    override suspend fun createResponse(builder: suspend MessageCreateBuilder.() -> Unit): EditableMessage {
        val messageBuilder = MessageCreateBuilder().apply { builder() }
        val response = ack.followUp {
            content = messageBuilder.content
            embeds = messageBuilder.embed?.let { mutableListOf(it.toRequest()) } ?: mutableListOf()
            allowedMentions = messageBuilder.allowedMentions?.build()
        }

        return EditableEphemeralFollowUp(response)
    }
}

class EditableEphemeralFollowUp(private val response: PublicFollowupMessage) : EditableMessage {
    override suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage {
        val messageBuilder = MessageModifyBuilder().apply { builder() }
        response.edit {
            content = messageBuilder.content
            embeds = messageBuilder.embed?.let { mutableListOf(it) } ?: mutableListOf()
            allowedMentions = messageBuilder.allowedMentions
        }
        return this
    }

    override suspend fun delete() = response.delete()
}

class SlashCommandContext(
    override val event: MessageCreateEvent,
    private val _command: Command<*>?,
    override val commands: Map<String, Command<*>>,
    override val processor: CommandProcessor,
    private val ack: PublicInteractionResponseBehavior
) : Context {

    override val command: Command<*>
        get() = _command ?: error("Command not available")

    internal val editableAck: EditableMessage by lazy { EditableAck(ack) }
    private var responded = false

    override suspend fun createResponse(builder: suspend MessageCreateBuilder.() -> Unit): EditableMessage {
        val messageBuilder = MessageCreateBuilder().apply { builder() }
        return if (responded) {
            ack.followUp {
                content = messageBuilder.content
                embeds = messageBuilder.embed?.let { mutableListOf(it.toRequest()) } ?: mutableListOf()
                allowedMentions = messageBuilder.allowedMentions?.build()
            }

            responded = true

            EditableFollowUp
        } else {
            editableAck.modify {
                content = messageBuilder.content
                embed = messageBuilder.embed
                allowedMentions = messageBuilder.allowedMentions
            }
        }
    }
}

private class EditableAck(private val ack: PublicInteractionResponseBehavior) : EditableMessage {
    override suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage {
        val messageBuilder = MessageModifyBuilder().apply { builder() }
        ack.edit {
            content = messageBuilder.content
            embeds = messageBuilder.embed?.let { mutableListOf(it) }
            allowedMentions = messageBuilder.allowedMentions
        }

        return this
    }

    override suspend fun delete() = ack.delete()
}

private object EditableFollowUp : EditableMessage {
    override suspend fun modify(builder: suspend MessageModifyBuilder.() -> Unit): EditableMessage =
        throw UnsupportedOperationException("You cannot edit follow ups")

    override suspend fun delete() {
        throw UnsupportedOperationException("You cannot delete follow ups")
    }
}
