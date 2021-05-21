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
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

interface Tag

@Serializable
data class TagEntry(
    @SerialName("_id")
    @Contextual
    val id: Id<TagEntry> = newId(),
    @Contextual
    val author: Snowflake,
    val name: String,
    val text: String,
    @Contextual
    val createdOn: LocalDateTime,
    val aliases: List<String> = emptyList()
) : Tag

infix fun TagEntry.calculateChangesTo(newTag: TagEntry): List<TagChange> {
    val changes = mutableListOf<TagChange>()

    if (this.author != newTag.author) {
        changes += AuthorChange(this.author, newTag.author)
    }

    if (this.name != newTag.name) {
        changes += NameChange(this.name, newTag.name)
    }

    if (this.text != newTag.text) {
        changes += TextChange(this.text, newTag.text)
    }

    if (this.aliases != newTag.aliases) {
        changes += AliasesChange(this.aliases, newTag.aliases)
    }

    return changes
}

suspend fun TagEntry.saveChanges(newTag: TagEntry, author: Snowflake?) {
    author ?: return

    val changes = this calculateChangesTo newTag
    val editAction =
        EditAction(
            author,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            changes
        )
    BankoBot.repositories.tagActions.save(editAction)
}

suspend fun TagEntry.saveCreation(author: Snowflake?) {
    author ?: return

    val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val createAction = CreateAction(author, time, this.id, this.name, this.text)
    BankoBot.repositories.tagActions.save(createAction)
}

suspend fun TagEntry.saveDeletion(author: Snowflake?) {
    author ?: return

    val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val deleteAction = DeleteAction(author, time, name)
    BankoBot.repositories.tagActions.save(deleteAction)
}
