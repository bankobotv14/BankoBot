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

import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.x.commands.argument.Argument
import de.nycode.bankobot.command.slashcommands.arguments.AbstractSlashCommandArgument
import java.util.*

/**
 * [Argument] which is compatible with slash commands.
 *
 * @property description the description of the argument
 * @property required whether the property is required or not
 *
 * @see Argument
 * @see AbstractSlashCommandArgument
 */
@OptIn(KordPreview::class)
interface SlashArgument<T, CONTEXT> : Argument<T, CONTEXT> {
    val description: String
    val required: Boolean
    override val name: String
        get() = name.lowercase(Locale.getDefault())

    /**
     * Applies the argument configuration to the [BaseApplicationBuilder]
     */
    fun BaseApplicationBuilder.applyArgument()
}
