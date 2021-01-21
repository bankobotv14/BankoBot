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

import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.createMessage
import de.nycode.bankobot.utils.HastebinUtil
import de.nycode.bankobot.config.Environment
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.processor.CommandProcessor
import dev.kord.x.commands.model.processor.ErrorHandler
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.coroutineContext

/**
 * Implementation of [ErrorHandler] which just sends a generic error message.
 *
 * Used in [Environment.DEVELOPMENT]
 */
object DebugErrorHandler : ErrorHandler<MessageCreateEvent, MessageCreateEvent, KordCommandEvent> {

    override suspend fun CommandProcessor.exceptionThrown(
        event: MessageCreateEvent,
        command: Command<KordCommandEvent>,
        exception: Exception
    ) {
        event.message.channel.createMessage("An error occurred please read the logs")
    }
}

/**
 * Implementation of [ErrorHandler] that reports an error log to hastebin.
 *
 * @see HastebinUtil
 */
object HastebinErrorHandler :
    ErrorHandler<MessageCreateEvent, MessageCreateEvent, KordCommandEvent> {
    override suspend fun CommandProcessor.exceptionThrown(
        event: MessageCreateEvent,
        command: Command<KordCommandEvent>,
        exception: Exception
    ) {
        event.message.channel.createMessage(
            Embeds.loading(
                "Ein Fehler ist aufgetreten!",
                "Bitte warte einen Augenblick, während ich versuche mehr Informationen über den Fehler herauszufinden"
            )
        ).edit {
            val hastebinLink =
                HastebinUtil.postToHastebin(
                    collectErrorInformation(
                        exception,
                        event.message,
                        Thread.currentThread()
                    )
                )
            embed = Embeds.error(
                "Ein Fehler ist aufgetreten!",
                "Bitte senden [diesen]($hastebinLink) Link an einen Entwickler"
            )
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext") // How is StringBuilder#append() blocking?!
    private suspend fun collectErrorInformation(
        e: Exception,
        context: Message,
        thread: Thread,
    ): String {
        val information = StringBuilder()
        val channel = context.channel.asChannel()
        val guild = context.getGuild()
        information.append("Guild: ").append(guild.name).append('(').append(guild.id.value)
            .appendLine(')')
        val executor = context.author
        information.append("Executor: ").append('@').append(executor?.tag).append('(')
            .append(executor?.id?.value)
            .appendLine(')')
        val selfMember = guild.getMember(context.kord.selfId)
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
