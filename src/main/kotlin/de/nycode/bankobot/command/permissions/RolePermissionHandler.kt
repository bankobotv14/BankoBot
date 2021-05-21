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

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import dev.kord.common.entity.Snowflake
import dev.kord.core.any
import dev.kord.core.entity.Member
import kotlinx.coroutines.runBlocking

/**
 * Implementation of [PermissionHandler] which uses Discord roles.
 *
 * [PermissionLevel.ALL] requires nothing
 * [PermissionLevel.BOT_OWNER] requires being a member of the Discord application team.
 * [PermissionLevel.ADMIN] requires [Config.ADMIN_ROLE]
 * [PermissionLevel.MODERATOR] requires [Config.MODERATOR_ROLE] or [Config.ADMIN_ROLE]
 *
 * Used with [Environment.PRODUCTION]
 *
 * @see Config.ADMIN_ROLE
 * @see Config.MODERATOR_ROLE
 */
object RolePermissionHandler : AbstractPermissionHandler() {

    private val modRole =
        Config.MODERATOR_ROLE ?: error("Please define permission roles in env config")
    private val adminRole =
        Config.ADMIN_ROLE ?: error("Please define permission roles in env config")

    private val botOwners by lazy {
        runBlocking {
            val application = BankoBot.kord.getApplicationInfo()
            application.team?.run { members.map { it.userId } } ?: listOf(application.ownerId)
        }
    }

    override suspend fun isCovered(member: Member, permission: PermissionLevel): Boolean {
        return when (permission) {
            PermissionLevel.ALL -> true
            PermissionLevel.BOT_OWNER -> member.id in botOwners
            PermissionLevel.MODERATOR -> member.hasRole(modRole, adminRole)
            PermissionLevel.ADMIN -> member.hasRole(adminRole)
        }
    }
}

suspend fun Member.hasRole(vararg roleIds: Snowflake) = roles.any { id in roleIds }
