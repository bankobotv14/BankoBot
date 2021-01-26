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
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.permissions.PermissionLevel
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.utils.*
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.core.entity.Member
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.primitive.IntArgument
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.kord.model.KordEvent
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import io.ktor.client.request.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.search
import org.litote.kmongo.coroutine.aggregate

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagCommand(): CommandSet = command("tag") {
    alias("t")
    description("Einen Tag anzeigen")

    invoke(WordArgument.named("tag")) { tagName ->
        val tag = findTag(tagName) ?: return@invoke
        respond(tag.text)

        val useAction = UseAction(
            author = message.author?.id ?: return@invoke,
            date = Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            tagId = tag.id
        )
        BankoBot.repositories.tagActions.save(useAction)
    }
}

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

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun deleteTagCommand(): CommandSet = command("delete-tag") {
    description("Tag löschen")
    alias("remove-tag")

    invoke(WordArgument.named("tag")) { tagName ->
        val tag = findTag(tagName) ?: return@invoke

        if (tag.author != author.id && message.getAuthorAsMember()?.hasDeletePermission() != true) {
            respondEmbed(
                Embeds.error(
                    "Du bist nicht der Autor.",
                    "Du darfst diesen Tag nicht löschen, da du ihn nicht erstellt hast!"
                )
            )
        } else {
            doExpensiveTask("Tag wird gelöscht") {
                BankoBot.repositories.tag.deleteOneById(tag.id)
                editEmbed(
                    Embeds.success(
                        "Tag wurde gelöscht!",
                        "Du hast den Tag **${tagName.trim()}** erfolgreich gelöscht!"
                    )
                )
            }
        }
    }
}

private suspend fun Member.hasDeletePermission(): Boolean {
    return BankoBot.permissionHandler.isCovered(this, PermissionLevel.MODERATOR) ||
            BankoBot.permissionHandler.isCovered(this, PermissionLevel.BOT_OWNER)
}

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun createAliasCommand(): CommandSet = command("create-alias") {
    description("Alias erstellen.")

    invoke(WordArgument.named("tag"), WordArgument.named("alias")) { tagName, aliasName ->
        val tag = findTag(tagName) ?: return@invoke

        val aliasTag = BankoBot.repositories.tag.findOne(TagEntry::aliases contains aliasName)
        if (aliasTag != null) {
            respondEmbed(
                Embeds.error(
                    "Alias existiert bereits!",
                    "Du kannst diesen Alias nicht erstellen, da der Tag **${aliasTag.name}** diesen bereits nutzt!"
                )
            )
            return@invoke
        }

        val aliasNameTag = BankoBot.repositories.tag.findOne(TagEntry::name eq aliasName)
        if (aliasNameTag != null) {
            respondEmbed(
                Embeds.error(
                    "Name bereits genutzt!",
                    "Du kannst diesen Alias nicht erstellen," +
                            " da der Tag **${aliasNameTag.name}** diesen bereits als Namen nutzt!"
                )
            )
            return@invoke
        }

        val loadingMessage = respondEmbed(Embeds.loading("Alias wird erstellt", null))

        val newTag = tag.copy(aliases = tag.aliases.toMutableList().apply {
            add(aliasName.trim())
        }.toList())

        BankoBot.repositories.tag.save(newTag)

        loadingMessage.editEmbed(Embeds.success("Alias wurde erstellt!", "Du hast den Alias **$aliasName** erstellt!"))
    }
}

private fun notFound() = Embeds.error("Unbekannter Tag!", "Dieser Tag konnte nicht gefunden werden!")

@Suppress("MagicNumber")
@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun listTagsCommand(): CommandSet = command("list-tags") {
    description("Alle Tags anzeigen")

    invoke(IntArgument.named("page").optional(1), IntArgument.named("pageSize").optional(8)) { page, pageSize ->
        val tagCount = BankoBot.repositories.tag.countDocuments().toInt()

        LazyItemProvider(tagCount) { start, _ ->
            BankoBot.repositories.tag.find()
                .paginate(start, pageSize) {
                    it.name
                }
        }.paginate(message.channel, "Tags") {
            firstPage = page
            itemsPerPage = pageSize
        }
    }
}

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun editTagCommand(): CommandSet = command("edit-tag") {
    description("Einen Tag editieren")

    invoke(WordArgument.named("tag"), StringArgument.named("newtext")) { tagName, newText ->
        val tag = findTag(tagName) ?: return@invoke

        if (tag.text == newText) {
            respondEmbed(
                Embeds
                    .error("Text stimmt überein", "Der angegebene Text stimmt mit dem aktuellen Text des Tags überein!")
            )
            return@invoke
        }

        doExpensiveTask("Tag wird editiert") {
            val newTag = tag.copy(text = newText)
            BankoBot.repositories.tag.save(newTag)
            editEmbed(Embeds.success("Tag wurde editiert!", "Der Tag wurde erfolgreich aktualisiert!"))
        }
    }
}

@Suppress("MagicNumber")
@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagsFromUserCommand(): CommandSet = command("from-user") {
    alias("from")

    invoke(MemberArgument) { member ->

        val tagCount = BankoBot.repositories.tag.countDocuments(TagEntry::author eq member.id).toInt()

        if (tagCount == 0) {
            respondEmbed(Embeds.error("Keine Tags gefunden!", "${member.mention} hat keine Tags erstellt!"))
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

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun searchTagsCommand(): CommandSet = command("search") {
    alias("s")

    invoke(WordArgument) { search ->
        val tags = searchTags(search)
        LazyItemProvider(tags.size) { start, end ->
            tags.subList(start, end + 1)
                .map { it.name }
        }.paginate(message.channel, "Tag-Suche \"$search\"")
    }
}

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

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagRawCommand(): CommandSet = command("tag-raw") {
    alias("raw")

    invoke(WordArgument) { tagName ->
        val tag = findTag(tagName) ?: return@invoke

        val url = HastebinUtil.postToHastebin(tag.text)
        respondEmbed(Embeds.info("Raw-Inhalt", "Den Raw-Inhalt vom Tag \"${tag.name}\" findest du [hier]($url)"))
    }
}

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagInfoCommand(): CommandSet = command("tag-info") {

    invoke(WordArgument) { tagName ->
        val tag = findTag(tagName) ?: return@invoke
        val uses = BankoBot.repositories.tagActions.countDocuments(UseAction::tagId eq tag.id).toInt()

        val author = kord.getUser(tag.author)
        val authorName = author?.username ?: "User nicht gefunden!"

        respondEmbed(Embeds.info("Tag-Informationen") {
            author {
                icon = author?.avatar?.defaultUrl
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
        })
    }
}

private suspend fun KordEvent.findTag(tagName: String): TagEntry? {
    val tag = BankoBot.repositories.tag.findOne(TagEntry::name eq tagName)

    if (tag == null) {
        respondEmbed(notFound())
    }
    return tag
}
