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
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.processor.CommandProcessor
import dev.kord.x.commands.model.processor.ErrorHandler
import dev.kord.x.commands.model.processor.ProcessorContext

/**
 * Command context for slash command.
 *
 * @see InteractionEventSource
 */
@OptIn(KordPreview::class)
object InteractionContext :
    ProcessorContext<InteractionCreateEvent, MessageCreateEvent, KordCommandEvent>

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
