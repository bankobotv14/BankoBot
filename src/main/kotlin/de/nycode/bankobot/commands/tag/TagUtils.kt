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
