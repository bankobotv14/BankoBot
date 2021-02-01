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
