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
