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

package de.nycode.bankobot.commands.tag.commands

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.command
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.TagArgument
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.commands.tag.UseAction
import de.nycode.bankobot.commands.tag.checkEmpty
import de.nycode.bankobot.utils.Embeds
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import kotlinx.datetime.toJavaLocalDateTime
import org.litote.kmongo.eq
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagInfoCommand(): CommandSet = command("tag-info") {

    invoke(TagArgument) { tag ->
        if (checkEmpty(tag)) {
            return@invoke
        }

        tag as TagEntry

        val uses = BankoBot.repositories.tagActions.countDocuments(UseAction::tagId eq tag.id).toInt()

        val author = kord.getUser(tag.author)
        val authorName = author?.username ?: "User nicht gefunden!"

        val rank = tag.getRank()

        sendResponse(Embeds.info("Tag-Informationen") {
            author {
                icon = author?.avatar?.url
                name = authorName
            }
            field {
                name = "Erstellt von"
                value = authorName
                inline = true
            }
            field {
                name = "Benutzungen"
                value = "$uses"
                inline = true
            }
            field {
                name = "Rang"
                value = "$rank"
                inline = true
            }
            field {
                name = "Erstellt"
                value = tag.createdOn.toJavaLocalDateTime().format(dateTimeFormatter)
            }
            if (tag.aliases.isNotEmpty()) {
                field {
                    name = "Aliase"
                    value = tag.aliases.joinToString()
                }
            }
        })
    }
}

@Suppress("MagicNumber")
private suspend fun TagEntry.getRank(): Int {

    val uses = BankoBot.repositories.tagActions.countDocuments(UseAction::tagId eq this.id)

    return BankoBot.repositories.tag.find()
        .toList()
        .filter { it.getUses() > uses }
        .count() + 1
}

private suspend fun TagEntry.getUses(): Int {
    return BankoBot.repositories.tagActions.countDocuments(UseAction::tagId eq this.id).toInt()
}
