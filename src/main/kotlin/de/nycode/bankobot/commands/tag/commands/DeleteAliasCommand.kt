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

package de.nycode.bankobot.commands.tag.commands

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.*
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
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
            respondEmbed(
                Embeds.error(
                    "Nicht gefunden!",
                    "Es konnte kein Tag mit dem Alias \"$aliasName\" gefunden werden!"
                )
            )
            return@invoke
        }

        if (tag.author != message.author?.id && message.getAuthorAsMember()?.hasDeletePermission()?.not() == true) {
            respondEmbed(
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
