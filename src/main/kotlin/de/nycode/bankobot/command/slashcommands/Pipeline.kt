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

package de.nycode.bankobot.command.slashcommands

import de.nycode.bankobot.command.slashcommands.KordInteractionErrorHandler.exceptionThrown
import de.nycode.bankobot.command.slashcommands.KordInteractionErrorHandler.rejectArgument
import de.nycode.bankobot.utils.EMBED_TITLE_MAX_LENGTH
import de.nycode.bankobot.utils.limit
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.common.entity.optional.optional
import dev.kord.common.entity.optional.optionalSnowflake
import dev.kord.core.cache.data.MessageData
import dev.kord.core.cache.data.ReactionData
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.Command
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.processor.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant

/**
 * Command context for slash command.
 *
 * @see InteractionEventSource
 */
@OptIn(KordPreview::class)
object InteractionContext :
    ProcessorContext<InteractionCreateEvent, MessageCreateEvent, KordCommandEvent>

@OptIn(KordPreview::class)
private fun Command.buildCommandString(): String {
    val builder = StringBuilder()
    builder.append(name) // invoke
    if (options.isNotEmpty()) {
        builder.append(
            options.values.joinToString(prefix = " ", separator = " ") { it.value.toString() } // arguments
        )
    }
    return builder.toString()
}

/**
 * [ContextConverter] for slash commands ([InteractionCreateEvent]).
 */
@OptIn(KordPreview::class)
object InteractionContextConverter :
    ContextConverter<InteractionCreateEvent, MessageCreateEvent, KordCommandEvent> {
    @OptIn(KordPreview::class)
    override val InteractionCreateEvent.text: String
        get() = interaction.command.buildCommandString()

    override fun InteractionCreateEvent.toArgumentContext(): MessageCreateEvent {
        runBlocking { interaction.acknowledge(true) }
        val messageData = MessageData(
            interaction.id,
            interaction.channelId,
            interaction.guildId.optionalSnowflake(),
            runBlocking { interaction.member.asUser().data },
            text,
            Clock.System.now().toJavaInstant().toString(),
            null,
            tts = false,
            mentionEveryone = false,
            emptyList(),
            emptyList(),
            emptyList<Snowflake>().optional(),
            emptyList(),
            emptyList(),
            emptyList<ReactionData>().optional(),
            Optional.Missing(),
            false,
            OptionalSnowflake.Missing,
            MessageType.Unknown,
        )
        val message = Message(messageData, interaction.kord, interaction.supplier)
        val member = runBlocking { interaction.member.asMember() }
        return MessageCreateEvent(message, interaction.guildId, member, shard, interaction.supplier)
    }

    override fun MessageCreateEvent.toCommandEvent(data: CommandEventData<KordCommandEvent>): KordCommandEvent {
        return KordCommandEvent(this, data.command, data.commands, data.koin, data.processor)
    }
}

/**
 * Default implementation of [ErrorHandler] for slash commands.
 * only implements [exceptionThrown] and [rejectArgument] since all other events shouldn't happen
 */
@OptIn(KordPreview::class)
object KordInteractionErrorHandler :
    ErrorHandler<InteractionCreateEvent, MessageCreateEvent, KordCommandEvent> {

    override suspend fun CommandProcessor.rejectArgument(
        rejection: ErrorHandler.RejectedArgument<InteractionCreateEvent,
                MessageCreateEvent,
                KordCommandEvent>,
    ) {
        rejection.event.interaction.channel.createMessage(rejection.message)
    }

    override suspend fun CommandProcessor.exceptionThrown(
        event: InteractionCreateEvent,
        command: dev.kord.x.commands.model.command.Command<KordCommandEvent>,
        exception: Exception,
    ) {
        event.interaction.channel.createMessage(exception.stackTraceToString().limit(
            EMBED_TITLE_MAX_LENGTH))
    }
}
