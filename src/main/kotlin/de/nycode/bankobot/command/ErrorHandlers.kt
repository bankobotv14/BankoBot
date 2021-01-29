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

@Suppress("UnnecessaryAbstractClass")
abstract class AbstractErrorHandler :
    ErrorHandler<MessageCreateEvent, MessageCreateEvent, KordCommandEvent> by kordHandler {
    override suspend fun CommandProcessor.rejectArgument(
        rejection: ErrorHandler.RejectedArgument<MessageCreateEvent,
                MessageCreateEvent,
                KordCommandEvent>,
    ) {
        if (rejection.message == "Expected more input but reached end.") {
            rejection.event.message.channel.createEmbed(Embeds.command(rejection.command, this))
        } else with(kordHandler) { rejectArgument(rejection) }
    }
}

object DebugErrorHandler : AbstractErrorHandler() {
    override suspend fun CommandProcessor.exceptionThrown(
        event: MessageCreateEvent,
        command: Command<KordCommandEvent>,
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
        command: Command<KordCommandEvent>,
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

    @Suppress("BlockingMethodInNonBlockingContext") // How is StringBuilder#append() blocking?!
    suspend fun collectErrorInformation(
        e: Exception,
        content: String,
        channel: MessageChannelBehavior,
        guild: Guild,
        executor: Member,
        thread: Thread,
        coroutine: CoroutineContext? = null
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
