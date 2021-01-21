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
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.core.entity.Member
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.or

@PublishedApi
@AutoWired
@ModuleName(TagModule)
internal fun tagCommand(): CommandSet = command("tag") {
    alias("t")
    description("Einen Tag anzeigen")

    invoke(WordArgument.named("tag")) { tagName ->
        val tag = BankoBot.repositories.tag.findOne(or(TagEntry::name eq tagName, TagEntry::aliases contains tagName))
        if (tag == null) {
            respondEmbed(notFound())
        } else {
            respond(tag.toString())
        }
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
            val entry = TagEntry(author = author.id, name = tagName.trim(), text = tagText)
            val loadingEmbed =
                respondEmbed(Embeds.loading("Tag wird erstellt", "Erstelle den Tag '${tagName.trim()}'!"))
            BankoBot.repositories.tag.save(entry)
            loadingEmbed.editEmbed(
                Embeds.success(
                    "Tag wurde erstellt",
                    "Du hast den Tag **${tagName.trim()}** erfolgreich erstellt!"
                )
            )
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
        val tag = BankoBot.repositories.tag.findOne(TagEntry::name eq tagName)
        if (tag != null) {
            if (tag.author != author.id && message.getAuthorAsMember()?.hasDeletePermission() != true) {
                respondEmbed(
                    Embeds.error(
                        "Du bist nicht der Autor.",
                        "Du darfst diesen Tag nicht löschen, da du ihn nicht erstellt hast!"
                    )
                )
            } else {
                val loadingEmbed = respondEmbed(Embeds.loading("Tag wird gelöscht", null))
                BankoBot.repositories.tag.deleteOneById(tag.id)
                loadingEmbed.editEmbed(
                    Embeds.success(
                        "Tag wurde gelöscht!",
                        "Du hast den Tag **${tagName.trim()}** erfolgreich gelöscht!"
                    )
                )
            }
        } else {
            respondEmbed(notFound())
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
        val tag = BankoBot.repositories.tag.findOne(TagEntry::name eq tagName)

        if (tag == null) {
            respondEmbed(notFound())
            return@invoke
        }

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
                    "Du kannst diesen Alias nicht erstellen, da der Tag **${aliasNameTag.name}** diesen bereits als Namen nutzt!"
                )
            )
            return@invoke
        }

        val loadingMessage = respondEmbed(Embeds.loading("Alias wird erstellt", null))
        val newTag = tag.copy(aliases = listOf(aliasName.trim(), *tag.aliases.toTypedArray()))
        BankoBot.repositories.tag.save(newTag)
        loadingMessage.editEmbed(Embeds.success("Alias wurde erstellt!", "Du hast den Alias **$aliasName** erstellt!"))
    }
}

private fun notFound() = Embeds.error("Unbekannter Tag!", "Dieser Tag konnte nicht gefunden werden!")
