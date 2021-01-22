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

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.on
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.kord.model.processor.KordContextConverter
import dev.kord.x.commands.model.processor.ContextConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * Implementation of [ContextConverter] which sends typing before replying to a command.
 *
 * Most methods delegate to [KordContextConverter]
 * @see KordContextConverter
 * @see ContextConverter
 */
object BankoBotContextConverter :
    ContextConverter<MessageCreateEvent, MessageCreateEvent, KordCommandEvent> by KordContextConverter {

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
            withTimeout(5.seconds) {
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
}
