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
import de.nycode.bankobot.command.permissions.PermissionLevel
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.commands.tag.saveCreation
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

private val regex = "\\\$\\(([a-zA-Z0-9_-]+) ?(.+)?\\)".toRegex()

@PublishedApi
@AutoWired
@ModuleName(TagModule)
@Suppress("UnsafeCallOnNullableType")
internal fun createTagCommand(): CommandSet = command("create-tag") {
    description("Tag erstellen")

    invoke(
        WordArgument.named("Name")
            .asSlashArgument("Name des Tags"),
        StringArgument.named("Text")
            .asSlashArgument("Text des Tags")
    ) { tagName, tagText ->
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
                var text = tagText

                if (!BankoBot.permissionHandler.isCovered(message.getAuthorAsMember()!!, PermissionLevel.BOT_OWNER)) {
                    regex.findAll(text).forEach {
                        text = text.replace(it.value, "\\${it.value}")
                    }
                }

                val entry = TagEntry(
                    author = author.id,
                    name = tagName.trim(),
                    text = text,
                    createdOn = Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    )
                )
                BankoBot.repositories.tag.save(entry)

                entry.saveCreation(message.author?.id)

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
