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

import de.nycode.bankobot.command.description
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import dev.kord.rest.builder.interaction.ApplicationCommandsCreateBuilder
import dev.kord.x.commands.model.command.CommandBuilder

/**
 * Class containing all necessary information to register a slash command
 * @property name the name
 * @property description the description
 * @property creator an extension of [ApplicationCommandCreateBuilder] to add options
 */
@OptIn(KordPreview::class)
class SlashCommandCreator(
    val name: String,
    val description: String,
    val creator: ApplicationCommandCreateBuilder.() -> Unit
) {
    fun ApplicationCommandsCreateBuilder.register() = command(name, description, creator)
}

/**
 * Converts this command into a [SlashCommandCreator].
 */
@OptIn(KordPreview::class)
fun CommandBuilder<*, *, *>.toSlashCommand(): SlashCommandCreator {
    val description = description ?: "<unknown description>"
    val creator: ApplicationCommandCreateBuilder.() -> Unit = {
        arguments.forEach {
            if (it is SlashArgument<*, *>) {
                with(it) {
                    applyArgument()
                }
            } else error("Command $name does use incompatible arguments for slashCommands: $it")
        }
    }

    return SlashCommandCreator(name, description, creator)
}
