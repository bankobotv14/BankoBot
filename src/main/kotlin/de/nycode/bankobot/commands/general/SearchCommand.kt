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

package de.nycode.bankobot.commands.general

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import de.nycode.bankobot.command.respond
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.GoogleUtil
import dev.schlaubi.mikbot.plugin.api.util.forList

class SearchCommand : Arguments() {
    val query by string {
        name = "Text"
        description = "Die Such-Query nach der gesucht werden soll"
    }
}

suspend fun GeneralModule.searchCommand() = publicSlashCommand(::SearchCommand) {
    name = "google"
    description = "Sucht dir deinen Scheiß aus dem Internet zusammen."


    action {
        search(arguments.query)
    }
}

private suspend fun PublicSlashCommandContext<*>.search(search: String) {
    val list = getResultAsList(search)
    if (list.isNullOrEmpty()) {
        respond(Embeds.error("Schade!", "Google möchte dir anscheinend nicht antworten! ._."))
    } else {
        editingPaginator {
            forList(user, list, { it }, { _, _ -> "Suchergebnisse" })
        }.send()
    }
}

suspend fun getResultAsList(search: String): List<String>? {
    val result = GoogleUtil.getResults(search) ?: return null
    return result.map { "**${it.title}**\n ${it.link} \n${it.snippet}" }
}
