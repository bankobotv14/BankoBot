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

import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.permissions.permission
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.kord.model.KordEvent
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
    suspend fun MessageChannelBehavior.createEmbed(
        base: EmbedBuilder,
        creator: suspend EmbedBuilder.() -> Unit = {}
    ): Message {
        return createMessage {
            creator(base)
            embeds.add(base)
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
            embeds = mutableListOf(base)
        }
    }

    /**
     * Responds to this command with an embed provided by [base] and applies [creator] to it
     */
    @Deprecated("Use sendResponse instead", ReplaceWith("sendResponse(base, creator)"), DeprecationLevel.ERROR)
    suspend fun KordEvent.respondEmbed(
        base: EmbedBuilder,
        creator: suspend EmbedBuilder.() -> Unit = {}
    ): Message = throw UnsupportedOperationException("This method is deprecated")
}
