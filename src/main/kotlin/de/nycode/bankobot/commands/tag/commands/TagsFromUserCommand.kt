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
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import de.nycode.bankobot.utils.LazyItemProvider
import de.nycode.bankobot.utils.paginate
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import org.litote.kmongo.eq

@Suppress("MagicNumber")
@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagsFromUserCommand(): CommandSet = command("from-user") {
    alias("from")

    invoke(
        MemberArgument.named("User").asSlashArgument("User")
    ) { member ->

        val tagCount = BankoBot.repositories.tag.countDocuments(TagEntry::author eq member.id).toInt()

        if (tagCount == 0) {
            sendResponse(Embeds.error("Keine Tags gefunden!", "${member.mention} hat keine Tags erstellt!"))
            return@invoke
        }

        val pageSize = 8

        LazyItemProvider(tagCount) { start, _ ->
            BankoBot.repositories.tag
                .find(TagEntry::author eq member.id)
                .paginate(start, pageSize) {
                    it.name
                }
        }.paginate(message.channel, "Tags von ${member.displayName}") {
            itemsPerPage = pageSize
        }
    }
}
