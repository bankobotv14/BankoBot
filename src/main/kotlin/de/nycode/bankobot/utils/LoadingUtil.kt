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

package de.nycode.bankobot.utils

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.EditableMessage
import de.nycode.bankobot.command.MessageEditableMessage
import de.nycode.bankobot.command.isSlashCommandContext
import de.nycode.bankobot.utils.Embeds.createEmbed
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.supplier.EntitySupplier
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.kord.model.KordEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Helper function that runs an expensive [task] in a coroutine and sends a loading message in [KordEvent.channel].
 *
 * @see MessageChannelBehavior.doExpensiveTask
 */
suspend fun Context.doExpensiveTask(
    statusTitle: String = "Bitte warten!",
    statusDescription: String? = null,
    task: suspend EditableMessage.() -> Unit,
) = doExpensiveTask(EmbedBuilder().apply {
    title = statusTitle
    description = statusDescription
}, task)

/**
 * Helper function that runs an expensive [task] in a coroutine and sends a loading message in [KordEvent.channel].
 *
 * @see MessageChannelBehavior.doExpensiveTask
 */
suspend fun Context.doExpensiveTask(
    loadingEmbedBuilder: EmbedBuilder,
    task: suspend EditableMessage.() -> Unit,
) {
    if (isSlashCommandContext()) {
        task(editableAck)
    } else {
        channel.doExpensiveTask(loadingEmbedBuilder) {
            MessageEditableMessage(this)
            task(MessageEditableMessage(this))
        }
    }
}

/**
 * Helper function that runs an expensive [task] in a coroutine and sends a loading message in this channel.
 */
suspend fun MessageChannelBehavior.doExpensiveTask(
    statusTitle: String = "Bitte warten!",
    statusDescription: String? = null,
    task: suspend MessageBehavior.() -> Unit,
) = doExpensiveTask(Embeds.loading(statusTitle, statusDescription), task)

/**
 * Helper function that runs an expensive [task] in a coroutine and sends a loading message in this channel.
 */
suspend fun MessageChannelBehavior.doExpensiveTask(
    loadingEmbedBuilder: EmbedBuilder,
    task: suspend MessageBehavior.() -> Unit,
) {
    coroutineScope {
        val message = async { createEmbed(loadingEmbedBuilder) }
        launch {
            task(
                AsyncMessageChannelBehavior(
                    kord,
                    supplier,
                    this@doExpensiveTask.id,
                    message,
                    coroutineContext
                )
            )
        }
    }
}

private class AsyncMessageChannelBehavior(
    override val kord: Kord,
    override val supplier: EntitySupplier,
    override val channelId: Snowflake,
    private val message: Deferred<Message>,
    private val coroutineContext: CoroutineContext,
) : MessageBehavior {
    override val id: Snowflake
        get() = message().id

    override suspend fun asMessage(): Message = message()

    override suspend fun asMessageOrNull(): Message = message()

    // In the end this class will only be used by MessageChannelBehavior.doExpensiveTask
    // Which only launches this tasks which can use this in an async coroutine
    // there for usage of runBlocking is okay as it only blocks an async coroutine
    // which can't do anything before the result of this Deferred is here
    private fun message() = runBlocking(coroutineContext) { message.await() }
}
