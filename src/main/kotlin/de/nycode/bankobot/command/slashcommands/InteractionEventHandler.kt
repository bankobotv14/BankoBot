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

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.common.entity.optional.optional
import dev.kord.common.entity.optional.optionalSnowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.InteractionResponseBehavior
import dev.kord.core.cache.data.MessageData
import dev.kord.core.cache.data.ReactionData
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.result.ArgumentResult
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.processor.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

private val LOG = KotlinLogging.logger {}

@OptIn(KordPreview::class)
data class InteractionErrorEvent constructor(
    val response: InteractionResponseBehavior,
    val interaction: Interaction,
    override val kord: Kord,
    override val shard: Int,
) : Event

@OptIn(KordPreview::class)
object InteractionEventHandler : EventHandler<InteractionCreateEvent> {
    override val context: ProcessorContext<InteractionCreateEvent, MessageCreateEvent, KordCommandEvent>
        get() = InteractionContext

    @OptIn(ExperimentalTime::class)
    override suspend fun CommandProcessor.onEvent(event: InteractionCreateEvent) {
        val filters = getFilters(context)
        if (!filters.all { it(event) }) return

        val interaction = event.interaction

        val ack = interaction.acknowledge(true)
        val errorEvent by lazy { InteractionErrorEvent(ack, interaction, event.kord, event.shard) }

        val commandName = interaction.command.name
        val command = getCommand(context, commandName) ?: return with(KordInteractionErrorHandler) {
            notFound(
                errorEvent,
                commandName
            )
        }

        val messageCreate = event.toMessageCreateEvent()

        @Suppress("UNCHECKED_CAST")
        val commandEvent = KordCommandEvent(messageCreate,
            command, commands, koin, this)

        @Suppress("UNCHECKED_CAST") val preconditions =
            getPreconditions(context)

        val passed = preconditions.sortedByDescending { it.priority }.all { it(commandEvent) }

        if (!passed) return

        @Suppress("UNCHECKED_CAST")
        val arguments =
            command.arguments as List<Argument<*, MessageCreateEvent>>
        val invoke by lazy { interaction.buildInvokeString() }

        val result =
            parseArguments(interaction.command, arguments, messageCreate)
        val (items) = when (result) {
            is ArgumentsResult.Success -> result
            is ArgumentsResult.Failure -> return with(KordInteractionErrorHandler) {
                val rejection = ErrorHandler.RejectedArgument(
                    command,
                    errorEvent,
                    invoke,
                    result.atChar,
                    result.argument,
                    result.failure.reason
                )
                rejectArgument(rejection)
            }
            is ArgumentsResult.TooManyWords -> error("Discord bunged up")
        }

        try {
            command.invoke(commandEvent, items)
        } catch (exception: Exception) {
            LOG.catching(exception)
            with(KordInteractionErrorHandler) { exceptionThrown(errorEvent, command, exception) }
        }
    }

    private suspend fun parseArguments(
        command: dev.kord.core.entity.interaction.Command,
        arguments: List<Argument<*, MessageCreateEvent>>,
        event: MessageCreateEvent,
    ): ArgumentsResult<MessageCreateEvent> {
        val items = mutableListOf<Any?>()
        var indexTrim = 2 + command.name.length // /<command>
        arguments.forEachIndexed { index, argument ->
            val option = command.options[argument.name.toLowerCase()]
            // each argument is prefix by " <name>:"
            indexTrim += argument.name.length + 2
            val argumentText =
                option?.value?.toString()
                    ?: "" // "" for optionals
            when (val result = argument.parse(argumentText, 0, event)) {
                is ArgumentResult.Success -> {
                    items += result.item
                    indexTrim += result.newIndex + 1 // space after the argument
                }
                is ArgumentResult.Failure -> return ArgumentsResult.Failure(
                    event,
                    result,
                    argument,
                    arguments,
                    index,
                    argumentText,
                    result.atChar + indexTrim
                )
            }
        }

        return ArgumentsResult.Success(items)
    }

    private fun InteractionCreateEvent.toMessageCreateEvent(): MessageCreateEvent {
        val messageData = MessageData(
            interaction.id,
            interaction.channelId,
            interaction.guildId.optionalSnowflake(),
            runBlocking { interaction.member.asUser().data },
            "<slash command invocation>",
            Clock.System.now().toJavaInstant().toString(),
            null,
            tts = false,
            mentionEveryone = false,
            emptyList(),
            emptyList(),
            emptyList<Snowflake>().optional(),
            emptyList(),
            emptyList(),
            emptyList<ReactionData>().optional(),
            Optional.Missing(),
            false,
            OptionalSnowflake.Missing,
            MessageType.Unknown,
        )
        val message = Message(messageData, interaction.kord, interaction.supplier)
        val member = runBlocking { interaction.member.asMember() }
        return MessageCreateEvent(message, interaction.guildId, member, shard, interaction.supplier)
    }
}

@OptIn(KordPreview::class)
private fun Interaction.buildInvokeString(): String {
    val builder = StringBuilder("/")
    builder.append(command.name) // command

    val options =
        command.options
            .toList()
            .joinToString(prefix = " ", separator = " ") { (name, value) ->
                "$name: ${value.value}"
            }
    builder.append(options)

    return builder.toString()
}
