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

package de.nycode.bankobot.commands.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.BlacklistEntry
import de.nycode.bankobot.command.respond
import de.nycode.bankobot.command.safeMember
import de.nycode.bankobot.utils.Embeds
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly

class BlacklistArguments : Arguments() {
    val member by member {
        name = "target"
        description = "Der User der auf die Blacklist gesetzt/von der Blacklist entfernt werden soll"
    }
}

suspend fun SettingsModule.blacklistCommand() = ephemeralSlashCommand(::BlacklistArguments) {
    name = "blacklist"
    guildAdminOnly()

    action {
        val entry = BankoBot.repositories.blacklist.findOneById(arguments.member.id.value)
        if (entry == null) {
            val newEntry = BlacklistEntry(safeMember.id)
            BankoBot.repositories.blacklist.save(newEntry)

            respond(
                Embeds.success(
                    "Hinzugefügt",
                    "${safeMember.mention} wurde erfolgreich auf die Blacklist gesetzt."
                )
            )
        } else {
            BankoBot.repositories.blacklist.deleteOneById(entry.userId)

            respond(
                Embeds.success(
                    "Entfernt",
                    "${safeMember.mention} wurde erfolgreich von der Blacklist entfernt."
                )
            )
        }
    }
}
