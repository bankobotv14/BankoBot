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

package de.nycode.bankobot.command.permissions

import de.nycode.bankobot.command.AbstractKordPrecondition
import de.nycode.bankobot.command.Context
import de.nycode.bankobot.utils.Embeds
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.command.CommandBuilder
import dev.kord.x.commands.model.command.CommandEvent
import dev.kord.x.commands.model.metadata.Metadata
import dev.kord.x.commands.model.precondition.Precondition

/**
 * Indicator on who can use a command.
 *
 * @see Command.permission
 * @see permission
 */
enum class PermissionLevel {
    /**
     * Default for all commands.
     * Everyone can run the command
     */
    ALL,

    /**
     * Only moderators and above can execute the command.
     */
    MODERATOR,

    /**
     * Only server admins can use the co
     */
    ADMIN,

    /**
     * Only bot developers can run the command.
     */
    BOT_OWNER
}

private object PermissionData : Metadata.Key<PermissionLevel>

/**
 * The permission of a command.
 *
 * @see PermissionLevel
 */
val Command<*>.permission: PermissionLevel
    get() = data.metadata[PermissionData] ?: PermissionLevel.ALL

/**
 * Sets the permission of a command to [permission].
 * @see Command.permission
 */
fun <S, A, COMMANDCONTEXT : CommandEvent>
        CommandBuilder<S, A, COMMANDCONTEXT>.permission(permission: PermissionLevel): Unit =
    metaData.set(PermissionData, permission)

/**
 * Abstract implementation that acts as a bridge between [PermissionHandler] and [Precondition]
 */
abstract class AbstractPermissionHandler : AbstractKordPrecondition(), PermissionHandler {
    override suspend fun invoke(event: CommandEvent): Boolean {
        require(event is Context)
        val member = event.event.member ?: error("Missing member")
        val command = event.command
        val permission = command.permission
        if (isCovered(member, permission)) return true

        event.sendResponse(
            Embeds.error(
                "Keine Berechtigung",
                "Du benötigst die Berechtigung $permission um diesen Befehl benutzen zu können"
            )
        )
        return false
    }
}
