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
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.commands.tag.hasDeletePermission
import de.nycode.bankobot.commands.tag.saveChanges
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import org.litote.kmongo.contains

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun deleteAliasCommand(): CommandSet = command("delete-alias") {
    invoke(WordArgument.named("Alias").asSlashArgument("Alias")) { aliasName ->
        val tag = BankoBot.repositories.tag.findOne(TagEntry::aliases contains aliasName)

        if (tag == null) {
            sendResponse(
                Embeds.error(
                    "Nicht gefunden!",
                    "Es konnte kein Tag mit dem Alias \"$aliasName\" gefunden werden!"
                )
            )
            return@invoke
        }

        if (tag.author != message.author?.id && message.getAuthorAsMember()?.hasDeletePermission()?.not() == true) {
            sendResponse(
                Embeds.error(
                    "Du bist nicht der Autor.",
                    "Du darfst diesen Alias nicht löschen, da du den Tag nicht erstellt hast!"
                )
            )
        } else {
            doExpensiveTask("Alias wird gelöscht...") {
                val newTag = tag.copy(aliases = tag.aliases
                    .toMutableList()
                    .apply {
                        remove(aliasName)
                    })
                BankoBot.repositories.tag.save(newTag)

                tag.saveChanges(newTag, message.author?.id)

                editEmbed(
                    Embeds.success(
                        "Alias wurde gelöscht!",
                        "Du hast den Alias \"$aliasName\" des Tags \"${tag.name}\" gelöscht!"
                    )
                )
            }
        }
    }
}
