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

import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.createEmbed
import de.nycode.bankobot.utils.HastebinUtil
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.kord.model.processor.KordErrorHandler
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.processor.CommandProcessor
import dev.kord.x.commands.model.processor.ErrorHandler
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val kordHandler = KordErrorHandler()

const val ERROR_MARKER = "[ERROR]"

@Suppress("UnnecessaryAbstractClass", "UNCHECKED_CAST")
abstract class AbstractErrorHandler :
    ErrorHandler<MessageCreateEvent, MessageCreateEvent, Context> {
    override suspend fun CommandProcessor.rejectArgument(
        rejection: ErrorHandler.RejectedArgument<MessageCreateEvent,
                MessageCreateEvent,
                Context>,
    ) {
        if (rejection.message == "Expected more input but reached end.") {
            rejection.event.message.channel.createEmbed(Embeds.command(rejection.command, this))
        } else with(kordHandler) {
            rejectArgument(
                rejection as ErrorHandler.RejectedArgument<MessageCreateEvent, MessageCreateEvent, KordCommandEvent>
            )
        }
    }
}

object DebugErrorHandler : AbstractErrorHandler() {
    override suspend fun CommandProcessor.exceptionThrown(
        event: MessageCreateEvent,
        command: Command<Context>,
        exception: Exception,
    ) {
        event.message.channel.createMessage("$ERROR_MARKER An error occurred please read the logs")
    }
}

/**
 * Implementation of [ErrorHandler] that reports an error log to hastebin.
 */
object HastebinErrorHandler : AbstractErrorHandler() {
    override suspend fun CommandProcessor.exceptionThrown(
        event: MessageCreateEvent,
        command: Command<Context>,
        exception: Exception,
    ) {
        event.message.channel.createMessage {
            // Pingy ping!
            content =
                "$ERROR_MARKER <@!419146440682766343> <@!416902379598774273> <@!449893028266770432>"
            embed = Embeds.loading(
                "Ein Fehler ist aufgetreten!",
                "Bitte warte einen Augenblick, während ich versuche mehr Informationen über den Fehler herauszufinden"
            )
        }.edit {
            val hastebinLink =
                HastebinUtil.postToHastebin(
                    collectErrorInformation(
                        exception,
                        event.message.content,
                        event.message.channel,
                        event.message.getGuild(),
                        @Suppress("UnsafeCallOnNullableType") // slash commands will always have a member
                        event.message.getAuthorAsMember()!!,
                        Thread.currentThread()
                    )
                )
            embed = Embeds.error(
                "Ein Fehler ist aufgetreten!",
                "Bitte senden [diesen]($hastebinLink) Link an einen Entwickler"
            )
        }
    }

    @Suppress(
        "BlockingMethodInNonBlockingContext",
        "LongParameterList"
    ) // How is StringBuilder#append() blocking?!
    suspend fun collectErrorInformation(
        e: Exception,
        content: String,
        channel: MessageChannelBehavior,
        guild: Guild,
        executor: Member,
        thread: Thread,
        coroutine: CoroutineContext? = null,
    ): String {
        val coroutineContext = coroutine ?: coroutineContext
        val kord = channel.kord
        val information = StringBuilder()
        information.append("Input: ").append(content).appendLine()
        information.append("Guild: ").append(guild.name).append('(').append(guild.id.value)
            .appendLine(')')
        information.append("Executor: ").append('@').append(executor.tag).append('(')
            .append(executor.id.value)
            .appendLine(')')
        val selfMember = guild.getMember(kord.selfId)
        information.append("Permissions: ").appendLine(selfMember.getPermissions().code)

        if (channel is TextChannel) {
            information.append("TextChannel: ").append('#').append(channel.name)
                .append('(').append(channel.id.value).appendLine(")")
            information.append("Channel permissions: ")
                .appendLine(channel.getEffectivePermissions(selfMember.id).code)
        }

        information.append("Timestamp: ")
            .appendLine(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        information.append("Thread: ").appendLine(thread)
        information.append("Coroutine: ").appendLine(coroutineContext)
        information.append("Stacktrace: ").appendLine().append(e.stackTraceToString())
        return information.toString()
    }
}
