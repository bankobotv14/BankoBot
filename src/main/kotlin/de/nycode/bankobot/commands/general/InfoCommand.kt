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

package de.nycode.bankobot.commands.general

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Emotes
import de.nycode.bankobot.utils.GitHubUtil
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke
import kotlinx.coroutines.async

@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun infoCommand() = command("info") {
    alias("i", "whoareyou")
    description("Zeigt Informationen über den Bot")

    invoke {
        sendInfo()
    }
}

/**
 * Sends an about message for the bot in this channel.
 * The developers field is fetched from the GitHub repository contributors
 */
suspend fun Context.sendInfo() = sendInfo({ sendResponse(it) }) { editEmbed(it) }

suspend inline fun <O> sendInfo(
    sender: (EmbedBuilder) -> O,
    editor: O.(EmbedBuilder) -> Unit
) {
    val embed: EmbedBuilder.() -> Unit = {
        field {
            name = "Programmiersprache"
            value = "[Kotlin](https://kotlinlang.org)"
            inline = true
        }

        field {
            name = "Prefix"
            value = "xd"
        }
    }

    val loading = EmbedBuilder().apply(embed).apply {
        field {
            name = "Entwickler"
            value = Emotes.LOADING
        }
    }
    val contributorsFuture = BankoBot.async { GitHubUtil.retrieveContributors() }
    val message = sender(loading)
    message.editor(EmbedBuilder().apply(embed).apply {
        val contributors = contributorsFuture.await()
        field {
            name = "Entwickler"
            value = contributors.joinToString(", ") {
                "[${it.login}](${it.htmlUrl})"
            }
        }
    })
}
