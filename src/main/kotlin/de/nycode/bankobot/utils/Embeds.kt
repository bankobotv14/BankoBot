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

import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.permissions.permission
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.kord.model.KordEvent
import dev.kord.x.commands.kord.model.respond
import dev.kord.x.commands.model.command.AliasInfo
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.processor.CommandProcessor

/**
 * Defines a creator of an embed.
 */
typealias EmbedCreator = EmbedBuilder.() -> Unit

/**
 * Some presets for frequently used embeds.
 */
@Suppress("unused", "TooManyFunctions")
object Embeds {

    /**
     * Creates a info embed with the given [title] and [description] and applies the [builder] to it.
     * @see EmbedCreator
     * @see EmbedBuilder
     */
    fun info(title: String, description: String? = null, builder: EmbedCreator = {}): EmbedBuilder =
        EmbedBuilder().apply {
            title(Emotes.INFO, title)
            this.description = description
            color = Colors.BLUE
        }.apply(builder)

    /**
     * Creates a success embed with the given [title] and [description] and applies the [builder] to it.
     * @see EmbedCreator
     * @see EmbedBuilder
     */
    fun success(
        title: String,
        description: String? = null,
        builder: EmbedCreator = {}
    ): EmbedBuilder =
        EmbedBuilder().apply {
            title(Emotes.SUCCESS, title)
            this.description = description
            color = Colors.LIGHT_GREEN
        }.apply(builder)

    /**
     * Creates a error embed with the given [title] and [description] and applies the [builder] to it.
     * @see EmbedCreator
     * @see EmbedBuilder
     */
    fun error(title: String, description: String?, builder: EmbedCreator = {}): EmbedBuilder =
        EmbedBuilder().apply {
            title(Emotes.ERROR, title)
            this.description = description
            color = Colors.LIGHT_RED
        }.apply(builder)

    /**
     * Creates a warning embed with the given [title] and [description] and applies the [builder] to it.
     * @see EmbedCreator
     * @see EmbedBuilder
     */
    fun warn(title: String, description: String?, builder: EmbedCreator = {}): EmbedBuilder =
        EmbedBuilder().apply {
            title(Emotes.WARN, title)
            this.description = description
            color = Colors.YELLOW
        }.apply(builder)

    /**
     * Creates a loading embed with the given [title] and [description] and applies the [builder] to it.
     * @see EmbedCreator
     * @see EmbedBuilder
     */
    fun loading(title: String, description: String?, builder: EmbedCreator = {}): EmbedBuilder =
        EmbedBuilder().apply {
            title(Emotes.LOADING, title)
            this.description = description
            color = Colors.DARK_BUT_NOT_BLACK
        }.apply(builder)

    /**
     * Creates a help embed for [command].
     */
    fun command(command: Command<*>, processor: CommandProcessor): EmbedBuilder {
        val children =
            processor.commands.filterValues { (it.aliasInfo as? AliasInfo.Child<*>)?.parent == command }.keys
        return info("${command.name} - Hilfe", command.description) {
            if (children.isNotEmpty()) {
                field {
                    name = "Aliase"
                    value = children.joinToString("`, `", "`", "`")
                }
            }
            field {
                name = "Usage"
                value = formatCommandUsage(command)
            }
            field {
                name = "Permission"
                value = command.permission.toString()
            }
//            addField("Permission", command.permission.name)
//            val subCommands = command.registeredCommands.map(::formatSubCommandUsage)
//            if (subCommands.isNotEmpty()) {
//                addField("Sub commands", subCommands.joinToString("\n"))
//            }
        }
    }

    private fun formatCommandUsage(command: Command<*>): String {
        val usage = command.arguments.joinToString(" ") {
            if ("optional" in it.toString()) "[${it.name}]" else "<${it.name}>"
        }

        return "!${command.name} $usage"
    }
//
//    private fun formatSubCommandUsage(command: AbstractSubCommand): String {
//        val builder = StringBuilder(Constants.firstPrefix)
//        builder.append(' ').append(command.name).append(' ').append(command.usage.replace("\n", "\\n"))
//
//        val prefix = " ${command.parent.name} "
//        builder.insert(Constants.firstPrefix.length, prefix)
//        builder.append(" - ").append(command.description)
//
//        return builder.toString()
//    }

    private fun EmbedBuilder.title(emote: String, title: String) {
        this.title = "$emote $title"
    }

    /**
     * Sends a new message in this channel containing the embed provided by [base] and applies [creator] to it
     */
    suspend fun MessageChannelBehavior.createMessage(
        base: EmbedBuilder,
        creator: suspend EmbedBuilder.() -> Unit = {}
    ): Message {
        return createMessage {
            creator(base)
            embed = base
        }
    }

    /**
     * Sends a new message in this channel containing the embed provided by [base] and applies [creator] to it
     */
    suspend fun MessageBehavior.editEmbed(
        base: EmbedBuilder,
        creator: suspend EmbedBuilder.() -> Unit = {}
    ): Message {
        return edit {
            creator(base)
            embed = base
        }
    }

    /**
     * Responds to this command with an embed provided by [base] and applies [creator] to it
     */
    suspend fun KordEvent.respondEmbed(
        base: EmbedBuilder,
        creator: suspend EmbedBuilder.() -> Unit = {}
    ): Message {
        return respond {
            creator(base)
            embed = base
        }
    }
}
