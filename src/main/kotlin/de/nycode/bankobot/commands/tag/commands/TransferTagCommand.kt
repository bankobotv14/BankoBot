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
import de.nycode.bankobot.commands.tag.TagArgument
import de.nycode.bankobot.commands.tag.hasDeletePermission
import de.nycode.bankobot.commands.tag.saveChanges
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.live.live
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.kord.model.respond
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take

@OptIn(KordPreview::class)
@PublishedApi
@AutoWired
@ModuleName(TagModule)
@Suppress("LongMethod")
internal fun transferTagCommand(): CommandSet = command("transfer") {
    invoke(
        TagArgument(),
        MemberArgument.named("Der neue Besitzer des Tags")
            .asSlashArgument("Der neue Besitzer des Tags")
    ) { tag, member ->

        if (tag.author != author.id && message.getAuthorAsMember()?.hasDeletePermission() != true) {
            respondEmbed(
                Embeds.error(
                    "Du bist nicht der Autor.",
                    "Du darfst diesen Tag nicht transferieren, da du ihn nicht erstellt hast!"
                )
            )
            return@invoke
        }

        respond {
            content = member.mention
            embed {
                description =
                    "${member.mention}, " +
                            "Bitte bestätige die Transferierung des Tags" +
                            " \"${tag.name}\" von ${message.author?.mention} zu ${member.mention}." +
                            "\n\n" +
                            "Um dies zu bestätigen, reagiere mit ${Emojis.whiteCheckMark} auf diese Nachricht!"
            }
        }.apply {
            addReaction(Emojis.whiteCheckMark)
            addReaction(Emojis.x)
            val reactionEvent = live().events.filterIsInstance<ReactionAddEvent>()
                .filter { it.user.id == member.id }
                .filter { it.emoji.name in arrayOf(Emojis.x.unicode, Emojis.whiteCheckMark.unicode) }
                .take(1)
                .single()

            when (reactionEvent.emoji.name) {
                Emojis.x.unicode -> {
                    deleteAllReactions()
                    editEmbed(
                        Embeds.error(
                            "Abgelehnt", "Die Transferierung des Tags" +
                                    " \"${tag.name}\" von ${message.author?.mention}" +
                                    " zu ${member.mention} wurde abgebrochen!"
                        )
                    )
                }
                Emojis.whiteCheckMark.unicode -> {
                    deleteAllReactions()
                    val newTag = tag.copy(author = member.id)
                    BankoBot.repositories.tag.save(newTag)

                    tag.saveChanges(newTag, author = message.author?.id)

                    editEmbed(
                        Embeds.success(
                            "Transfer erfolgreich!",
                            "Der Tag \"${tag.name}\" wurde erfolgreich von ${message.author?.mention}" +
                                    " zu ${member.mention} transferiert!"
                        )
                    )
                }
                else -> throw IllegalStateException("Invalid emoji")
            }
        }
    }
}
