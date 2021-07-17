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
