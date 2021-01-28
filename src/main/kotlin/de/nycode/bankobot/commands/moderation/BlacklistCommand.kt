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

package de.nycode.bankobot.commands.moderation

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.BlacklistEntry
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.ModerationModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.model.command.invoke

private val TargetArgument =
    MemberArgument.asSlashArgument("Der User der auf die Blacklist gesetzt/von der Blacklist entfernt werden soll")

@PublishedApi
@AutoWired
@ModuleName(ModerationModule)
internal fun blacklistCommand() = command("blacklist") {
    alias("bl", "schwarzeliste", "schwarze-liste")

    invoke(TargetArgument) { member ->
        val entry = BankoBot.repositories.blacklist.findOneById(member.id.value)
        if (entry == null) {
            val newEntry = BlacklistEntry(member.id)
            BankoBot.repositories.blacklist.save(newEntry)

            respondEmbed(
                Embeds.success(
                    "Hinzugefügt",
                    "${member.mention} wurde erfolgreich auf die Blacklist gesetzt."
                )
            )
        } else {
            BankoBot.repositories.blacklist.deleteOneById(entry.userId)

            respondEmbed(Embeds.success(
                "Entfernt",
                "${member.mention} wurde erfolgreich von der Blacklist entfernt."
            ))
        }
    }
}
