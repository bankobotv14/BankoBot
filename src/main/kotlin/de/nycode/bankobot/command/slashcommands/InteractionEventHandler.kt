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
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.common.entity.optional.optional
import dev.kord.common.entity.optional.optionalSnowflake
import dev.kord.core.Kord
import dev.kord.core.KordObject
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.cache.data.MessageData
import dev.kord.core.cache.data.ReactionData
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.result.ArgumentResult
import dev.kord.x.commands.model.processor.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

private val LOG = KotlinLogging.logger {}

@OptIn(KordPreview::class)
data class InteractionErrorEvent constructor(
    val context: Context,
    val interaction: GuildInteraction,
    override val kord: Kord,
    override val shard: Int,
) : Event

@OptIn(KordPreview::class)
object InteractionEventHandler : EventHandler<InteractionCreateEvent> {
    override val context: ProcessorContext<InteractionCreateEvent, MessageCreateEvent, Context>
        get() = InteractionContext

    @OptIn(ExperimentalTime::class)
    override suspend fun CommandProcessor.onEvent(event: InteractionCreateEvent) {
        val guildInteraction = event.interaction as? GuildInteraction ?: return
        val filters = getFilters(context)
        if (!filters.all { it(event) }) return
        val commandName = guildInteraction.command.rootName
        val foundCommand = getCommand(context, commandName)
        val ephemeral = foundCommand?.ephemeral ?: false
        val ack = if (ephemeral) guildInteraction.acknowledgeEphemeral() else guildInteraction.ackowledgePublic()
        val messageCreate by lazy { event.toMessageCreateEvent(guildInteraction) }
        val commandEvent by lazy {
            if (ephemeral) {
                EphemeralSlashCommandContext(
                    messageCreate, foundCommand, commands, this, ack as EphemeralInteractionResponseBehavior
                )
            } else {
                SlashCommandContext(
                    messageCreate, foundCommand, commands, this, ack as PublicInteractionResponseBehavior
                )
            }
        }
        val errorEvent by lazy { InteractionErrorEvent(commandEvent, guildInteraction, event.kord, event.shard) }

        val command = foundCommand ?: return with(KordInteractionErrorHandler) {
            notFound(
                errorEvent,
                commandName
            )
        }

        @Suppress("UNCHECKED_CAST") val preconditions =
            getPreconditions(context)

        val passed = preconditions.sortedByDescending { it.priority }.all { it(commandEvent) }

        if (!passed) return

        @Suppress("UNCHECKED_CAST")
        val arguments =
            command.arguments as List<Argument<*, MessageCreateEvent>>
        val invoke by lazy { guildInteraction.buildInvokeString() }

        val result =
            parseArguments(guildInteraction.command, arguments, messageCreate)
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
        command: InteractionCommand,
        arguments: List<Argument<*, MessageCreateEvent>>,
        event: MessageCreateEvent,
    ): ArgumentsResult<MessageCreateEvent> {
        val items = mutableListOf<Any?>()
        var indexTrim = 2 + command.rootName.length // /<command>
        arguments.forEachIndexed { index, argument ->
            val option = command.options[argument.name.toLowerCase()]
            // each argument is prefix by " <name>:"
            indexTrim += argument.name.length + 2
            val argumentValue =
                option?.value
            val argumentText = option.stringify()
            when (val result = argument.parse(argumentText, 0, event)) {
                is ArgumentResult.Success -> {
                    val item = result.item
                    items += if (item is KordObject && argumentText.isNotBlank()) {
                        argumentValue
                    } else {
                        item
                    }
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

    private fun InteractionCreateEvent.toMessageCreateEvent(guildInteraction: GuildInteraction): MessageCreateEvent {
        val messageData = MessageData(
            interaction.id,
            interaction.channelId,
            guildInteraction.guildId.optionalSnowflake(),
            runBlocking { guildInteraction.member.asUser().data },
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
            MessageType.Unknown(-1)
        )
        val message = Message(messageData, interaction.kord, interaction.supplier)
        val member = runBlocking { guildInteraction.member.asMember() }
        return MessageCreateEvent(message, guildInteraction.guildId, member, shard, interaction.supplier)
    }
}

@OptIn(KordPreview::class)
private fun Interaction.buildInvokeString(): String {
    val builder = StringBuilder("/")
    builder.append(command.rootName) // command

    val options =
        command.options
            .toList()
            .joinToString(prefix = " ", separator = " ") { (name, value) ->
                "$name: ${value.stringify()}"
            }
    builder.append(options)

    return builder.toString()
}

@OptIn(KordPreview::class)
fun OptionValue<*>?.stringify(): String {
    return when (val argumentValue = this?.value) {
        null -> ""
        is Member -> argumentValue.mention
        is User -> argumentValue.mention
        is TextChannel -> argumentValue.mention
        is Role -> argumentValue.mention
        else -> argumentValue.toString()
    }
}
