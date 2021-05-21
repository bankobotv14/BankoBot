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
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.*
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.litote.kmongo.contains
import org.litote.kmongo.eq

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun createAliasCommand(): CommandSet = command("create-alias") {
    description("Alias erstellen.")

    invoke(
        TagArgument,
        WordArgument.named("Alias").asSlashArgument("Alias")
    ) { tag, aliasName ->

        if (checkEmpty(tag)) {
            return@invoke
        }

        tag as TagEntry

        val aliasTag = BankoBot.repositories.tag.findOne(TagEntry::aliases contains aliasName)
        if (aliasTag != null) {
            sendResponse(
                Embeds.error(
                    "Alias existiert bereits!",
                    "Du kannst diesen Alias nicht erstellen, da der Tag **${aliasTag.name}** diesen bereits nutzt!"
                )
            )
            return@invoke
        }

        val aliasNameTag = BankoBot.repositories.tag.findOne(TagEntry::name eq aliasName)
        if (aliasNameTag != null) {
            sendResponse(
                Embeds.error(
                    "Name bereits genutzt!",
                    "Du kannst diesen Alias nicht erstellen," +
                            " da der Tag **${aliasNameTag.name}** diesen bereits als Namen nutzt!"
                )
            )
            return@invoke
        }

        doExpensiveTask("Alias wird erstellt...", null) {
            val newTag = tag.copy(aliases = tag.aliases.toMutableList().apply {
                add(aliasName.trim())
            }.toList())

            BankoBot.repositories.tag.save(newTag)

            message.author?.let { user ->
                val changes = tag calculateChangesTo newTag
                val editAction = EditAction(
                    user.id,
                    Clock.System.now()
                        .toLocalDateTime(
                            TimeZone.currentSystemDefault(),
                        ),
                    changes
                )
                BankoBot.repositories.tagActions.save(editAction)
            }

            editEmbed(Embeds.success("Alias wurde erstellt!", "Du hast den Alias **$aliasName** erstellt!"))
        }
    }
}
