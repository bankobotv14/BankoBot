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
