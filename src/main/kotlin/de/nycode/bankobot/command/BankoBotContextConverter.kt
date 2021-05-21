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

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.on
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.kord.model.processor.KordContextConverter
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.processor.CommandEventData
import dev.kord.x.commands.model.processor.ContextConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@Suppress("MagicNumber")
@OptIn(ExperimentalTime::class)
private val timeout = 5.seconds

/**
 * Implementation of [ContextConverter] which sends typing before replying to a command.
 *
 * Most methods delegate to [KordContextConverter]
 * @see KordContextConverter
 * @see ContextConverter
 */
object BankoBotContextConverter :
    ContextConverter<MessageCreateEvent, MessageCreateEvent, Context> {

    private val responses = mutableMapOf<Snowflake, MessageBehavior>()

    override fun MessageCreateEvent.toArgumentContext(): MessageCreateEvent {
        // This request has to finish before anything else happens
        // Otherwise we might respond with a message and have to wait
        // for the send typing timeout
        // hence the runBlocking
        runBlocking { message.channel.type() }
        awaitResponse()
        return this
    }

    fun Kord.messageDeleteListener() = on<MessageDeleteEvent> {
        responses.remove(messageId)?.delete()
    }

    @OptIn(ExperimentalTime::class)
    fun MessageCreateEvent.awaitResponse() {
        kord.launch {
            withTimeout(timeout) {
                val message = kord.events
                    .filterIsInstance<MessageCreateEvent>()
                    .map { it.message }
                    .filter { it.channelId == this@awaitResponse.message.channelId }
                    .filter { it.author?.id == kord.selfId }
                    .filter { ERROR_MARKER !in it.content }
                    .take(1)
                    .single()

                responses[this@awaitResponse.message.id] = message
            }
        }
    }

    override val MessageCreateEvent.text: String
        get() = with(KordContextConverter) { text }

    override fun MessageCreateEvent.toCommandEvent(data: CommandEventData<Context>): Context =
        createCommandEvent(this, data).toContext()
}

@Suppress("UNCHECKED_CAST")
private fun createCommandEvent(
    event: MessageCreateEvent,
    data: CommandEventData<*>
): KordCommandEvent {
    return KordCommandEvent(event, data.command as Command<KordCommandEvent>, data.commands, data.koin, data.processor)
}
