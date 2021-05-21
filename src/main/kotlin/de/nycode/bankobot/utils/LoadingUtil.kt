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
