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

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.ERROR_MARKER
import de.nycode.bankobot.command.HastebinErrorHandler
import de.nycode.bankobot.command.slashcommands.KordInteractionErrorHandler.exceptionThrown
import de.nycode.bankobot.command.slashcommands.KordInteractionErrorHandler.rejectArgument
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.HastebinUtil
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.respond
import dev.kord.x.commands.model.processor.CommandProcessor
import dev.kord.x.commands.model.processor.ErrorHandler
import dev.kord.x.commands.model.processor.ProcessorContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Command context for slash command.
 *
 * @see InteractionEventSource
 */
@OptIn(KordPreview::class)
object InteractionContext :
    ProcessorContext<InteractionCreateEvent, MessageCreateEvent, Context>

/**
 * Default implementation of [ErrorHandler] for slash commands.
 * only implements [exceptionThrown] and [rejectArgument] since all other events shouldn't happen
 */
@OptIn(KordPreview::class)
object KordInteractionErrorHandler :
    ErrorHandler<InteractionErrorEvent, MessageCreateEvent, Context> {

    private const val backtick = "`"
    private const val backtickEscape = "\u200E`"

    override suspend fun CommandProcessor.rejectArgument(
        rejection: ErrorHandler.RejectedArgument<InteractionErrorEvent,
                MessageCreateEvent,
                Context>,
    ) {
        with(rejection) {
            respondError(
                event,
                eventText,
                atChar,
                message
            )
        }
    }

    override suspend fun CommandProcessor.exceptionThrown(
        event: InteractionErrorEvent,
        command: dev.kord.x.commands.model.command.Command<Context>,
        exception: Exception,
    ) {
        val interaction = event.interaction
        val coroutine = coroutineContext
        coroutineScope {
            val logLink = async {
                HastebinUtil.postToHastebin(
                    HastebinErrorHandler.collectErrorInformation(
                        exception,
                        "<slash command invocation>",
                        interaction.channel.asChannel(),
                        interaction.guild.asGuild(),
                        interaction.member.asMember(),
                        Thread.currentThread(),
                        coroutine
                    )
                )
            }
            val loading = event.context.respond {
                // Pingy ping!
                content =
                    "$ERROR_MARKER <@!419146440682766343> <@!416902379598774273> <@!449893028266770432>"
                embed = Embeds.loading(
                    "Ein Fehler ist aufgetreten!",
                    "Bitte warte einen Augenblick, während ich versuche mehr Informationen" +
                            " über den Fehler herauszufinden"
                )
            }

            val hastebinLink = logLink.await()
            loading.delete()
            event.context.respond {
                content =
                    "$ERROR_MARKER <@!419146440682766343> <@!416902379598774273> <@!449893028266770432>"
                embed = Embeds.error(
                    "Ein Fehler ist aufgetreten!",
                    "Bitte senden [diesen]($hastebinLink) Link an einen Entwickler"
                )
            }
        }
    }

    private suspend inline fun respondError(
        event: InteractionErrorEvent,
        text: String,
        characterIndex: Int,
        message: String,
    ) {
        event.context.sendResponse(
            content = """
            <|>```
            <|>${text.replace(backtick, backtickEscape)}
            <|>${"-".repeat(characterIndex)}^ ${message.replace(backtick, backtickEscape)}
            <|>```
            """.trimMargin("<|>").trim()
        )
    }
}
