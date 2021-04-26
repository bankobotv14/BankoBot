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
import de.nycode.bankobot.command.description
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.*
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun deleteTagCommand(): CommandSet = command("delete-tag") {
    description("Tag löschen")
    alias("remove-tag")

    invoke(TagArgument) { tag ->
        if (checkEmpty(tag)) {
            return@invoke
        }

        tag as TagEntry

        if (tag.author != author.id && message.getAuthorAsMember()?.hasDeletePermission() != true) {
            sendResponse(
                Embeds.error(
                    "Du bist nicht der Autor.",
                    "Du darfst diesen Tag nicht löschen, da du ihn nicht erstellt hast!"
                )
            )
        } else {
            doExpensiveTask("Tag wird gelöscht") {
                BankoBot.repositories.tag.deleteOneById(tag.id)

                tag.saveDeletion(message.author?.id)

                editEmbed(
                    Embeds.success(
                        "Tag wurde gelöscht!",
                        "Du hast den Tag **${tag.name.trim()}** erfolgreich gelöscht!"
                    )
                )
            }
        }
    }
}
