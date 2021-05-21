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

package de.nycode.bankobot.commands.tag.commands

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.TagModule
import de.nycode.bankobot.commands.tag.*
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.Member
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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@OptIn(KordPreview::class, ExperimentalTime::class)
@PublishedApi
@AutoWired
@ModuleName(TagModule)
@Suppress("LongMethod", "MagicNumber")
internal fun transferTagCommand(): CommandSet = command("transfer") {
    val currentlyTransferring = mutableSetOf<String>()
    invoke(
        TagArgument,
        MemberArgument.named("Neuer-Besitzer")
            .asSlashArgument("Neuer-Besitzer")
    ) { tag, member ->

        if (checkEmpty(tag)) {
            return@invoke
        }

        tag as TagEntry

        if (tag.author != author.id && message.getAuthorAsMember()?.hasDeletePermission() != true) {
            sendResponse(
                Embeds.error(
                    "Du bist nicht der Autor.",
                    "Du darfst diesen Tag nicht transferieren, da du ihn nicht erstellt hast!"
                )
            )
            return@invoke
        }

        if (member.id == message.author?.id) {
            sendResponse(
                Embeds
                    .error("Nicht möglich!", "Du kannst den Tag nicht zu dir selbst transferieren!")
            )
            return@invoke
        }

        if (!currentlyTransferring.add(tag.name)) {
            sendResponse(
                Embeds
                    .error(
                        "Wird bereits transferiert!",
                        "Dieser Tag wird gerade transferiert! Bitte warte bis dies abgeschlossen ist!"
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
            try {
                withTimeout(45.seconds) {
                    val reactionEvent = live().events.filterIsInstance<ReactionAddEvent>()
                        .filter { it.user.id == member.id }
                        .filter { it.emoji.name in arrayOf(Emojis.x.unicode, Emojis.whiteCheckMark.unicode) }
                        .take(1)
                        .single()

                    when (reactionEvent.emoji.name) {
                        Emojis.x.unicode -> {
                            deleteAllReactions()
                            editEmbed(cancelMessage(tag, member))
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
            } catch (exception: TimeoutCancellationException) {
                kord.launch {
                    deleteAllReactions()
                    editEmbed(cancelMessage(tag, member))
                }
            }
            currentlyTransferring.remove(tag.name)
        }
    }
}

private fun Context.cancelMessage(
    tag: TagEntry,
    member: Member
) = Embeds.error(
    "Abgelehnt", "Die Transferierung des Tags" +
            " \"${tag.name}\" von ${message.author?.mention}" +
            " zu ${member.mention} wurde abgebrochen!"
)
