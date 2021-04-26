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

package de.nycode.bankobot.commands.tag

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.permissions.PermissionLevel
import de.nycode.bankobot.utils.Embeds
import dev.kord.core.entity.Member
import org.litote.kmongo.MongoOperator.search
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.eq

internal suspend fun Member.hasDeletePermission(): Boolean {
    return BankoBot.permissionHandler.isCovered(this, PermissionLevel.MODERATOR) ||
            BankoBot.permissionHandler.isCovered(this, PermissionLevel.BOT_OWNER)
}

internal fun notFound() = Embeds.error("Unbekannter Tag!", "Dieser Tag konnte nicht gefunden werden!")

@Suppress("MagicNumber")
internal suspend fun searchTags(searchTerm: String): List<TagEntry> {
    return BankoBot.repositories.tag.aggregate<TagEntry>(
        """[
  {
    $search: {
        index: 'default',
        text: {
            fuzzy: {
                maxEdits: 2
            },
        query: '$searchTerm',
        path: 'name'
        }
    }
  }
]"""
    )
        .toList()
}

internal suspend fun Context.findTag(tagName: String): TagEntry? {
    val tag = BankoBot.repositories.tag.findOne(TagEntry::name eq tagName)

    if (tag == null) {
        sendResponse(notFound(), {})
    }
    return tag
}
