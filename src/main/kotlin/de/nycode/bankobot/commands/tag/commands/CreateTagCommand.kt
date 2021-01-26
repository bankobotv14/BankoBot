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
import de.nycode.bankobot.commands.tag.CreateAction
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.litote.kmongo.eq

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun createTagCommand(): CommandSet = command("create-tag") {
    description("Tag erstellen")

    invoke(WordArgument.named("tag"), StringArgument.named("text")) { tagName, tagText ->
        val tag = BankoBot.repositories.tag.findOne(TagEntry::name eq tagName)

        if (tag != null) {
            respondEmbed(
                Embeds.error(
                    "Tag existierst bereits",
                    "Ein Tag mit dem Namen **$tagName** existiert bereits!"
                )
            )
        } else {
            doExpensiveTask("Tag wird erstellt", "Erstelle den Tag '${tagName.trim()}'!") {
                val entry = TagEntry(
                    author = author.id,
                    name = tagName.trim(),
                    text = tagText,
                    createdOn = Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    )
                )
                BankoBot.repositories.tag.save(entry)

                val createAction = CreateAction(
                    message.author!!.id,
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    entry.id,
                    entry.name,
                    entry.text
                )
                BankoBot.repositories.tagActions.save(createAction)

                editEmbed(
                    Embeds.success(
                        "Tag wurde erstellt",
                        "Du hast den Tag **${tagName.trim()}** erfolgreich erstellt!"
                    )
                )
            }
        }
    }
}
