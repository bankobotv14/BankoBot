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
