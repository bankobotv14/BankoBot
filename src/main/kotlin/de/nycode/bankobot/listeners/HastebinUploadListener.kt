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

package de.nycode.bankobot.listeners

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.utils.*
import de.nycode.bankobot.utils.Embeds.editEmbed
import dev.kord.core.Kord
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.model.command.invoke
import io.ktor.client.request.*

const val HASTEBIN_COMMAND = "hastebin"

@Suppress("MagicNumber")
@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun hastebinCommand() = command(HASTEBIN_COMMAND) {
    invoke(StringArgument.optional().asSlashArgument("Der hochzuladende String")) { text ->
        if (text == null && message.attachments.firstMessageTxtOrNull() == null) {
            sendResponse(
                Embeds.error(
                    "Kein Inhalt gefunden!",
                    "Ich konnte keinen Inhalt finden, " +
                            "den ich auf hastebin posten könnte!" +
                            "\n" +
                            "Bitte sende ihn als Text hinter dem Befehl oder als Textanhang!"
                )
            )
            return@invoke
        }

        if (text.isNullOrBlank()) {
            sendResponse(autoUpload(message))
        } else {
            sendResponse(autoUpload(text))
        }
    }
}

private fun Set<Attachment>.firstMessageTxtOrNull() = firstOrNull {
    !it.isImage && it.filename == "message.txt"
}

fun Kord.autoUploadListener() = on<MessageCreateEvent> {
    message.attachments.firstMessageTxtOrNull() ?: return@on
    message.channel.doExpensiveTask(makeEmbed()) {
        editEmbed(makeEmbed(autoUpload(message)))
    }
}

private suspend fun autoUpload(message: Message): String {
    val attachment = message.attachments.firstMessageTxtOrNull()
    return if (attachment != null && !attachment.isImage) {
        val text = BankoBot.httpClient.get<String>(attachment.url)
        autoUpload(text)
    } else {
        autoUpload(message.content)
    }
}

private suspend fun autoUpload(text: String): String {
    val codeBlock = text.toCodeBlock()
    val uploadText = codeBlock?.code ?: text
    val url = HastebinUtil.postToHastebin(uploadText)
    return if (codeBlock?.language != null) {
        url + ".${codeBlock.language}"
    } else {
        url
    }
}

private fun makeEmbed(url: String = Emotes.LOADING) = Embeds.warn(
    "Uff, das ist aber viel",
    """So viel Text willst du schreiben? | Benutze am besten einen Paste-Dienst wie ${Config.HASTE_HOST}.
            | Hier, ich mache das mal schnell für dich: $url
            |""".trimMargin()
)
