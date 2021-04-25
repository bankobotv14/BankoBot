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
