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

package de.nycode.bankobot.command.slashcommands.arguments

import de.nycode.bankobot.command.slashcommands.SlashArgument
import dev.kord.common.annotation.KordPreview
import dev.kord.core.KordObject
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.interaction.UserBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.kord.argument.UserArgument

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see UserBuilder
 * @see UserArgument
 */
@JvmName("asUserSlashArgument")
@OptIn(KordPreview::class)
fun <T : User?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    builder: UserBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> = UserSlashArgument(description, this, builder)

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see UserBuilder
 * @see MemberArgument
 */
@JvmName("asMemberSlashArgument")
@OptIn(KordPreview::class)
fun <T : Member?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    builder: UserBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> = UserSlashArgument(description, this, builder)

@OptIn(KordPreview::class)
private class UserSlashArgument<T : KordObject?, CONTEXT>(
    description: String,
    delegate: Argument<T, CONTEXT>,
    private val builder: UserBuilder.() -> Unit,
) : AbstractSlashCommandArgument<T, CONTEXT>(description, delegate) {
    override fun BaseApplicationBuilder.applyArgument() {
        user(name, description, builder.applyRequired())
    }
}
