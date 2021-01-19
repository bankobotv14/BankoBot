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
import de.nycode.bankobot.command.command
import de.nycode.bankobot.commands.ModerationModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.common.entity.Snowflake
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.model.command.invoke
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@AutoWired
@ModuleName(ModerationModule)
fun blacklistCommand() = command("blacklist") {
    alias("bl")

    invoke(MemberArgument) { user ->
        if (BankoBot.repositories.blacklistedUsers.findOneById(user.id) == null) {
            BankoBot.repositories.blacklistedUsers.save(BlacklistEntry(user.id))
            respondEmbed(
                Embeds.info(
                    "Zur Blacklist hinzugefügt",
                    "Der User ${user.mention} wurde erfolgreich zur Blacklist hinzugefügt"
                )
            )
        } else {
            BankoBot.repositories.blacklistedUsers.deleteOneById(user.id)
            respondEmbed(
                Embeds.info(
                    "Von der Blacklist entfernt",
                    "Der User ${user.mention} wurde erfolgreich von der Blacklist entfernt"
                )
            )
        }
    }
}

@Serializable
class BlacklistEntry(@SerialName("_id") val userId: Snowflake)
