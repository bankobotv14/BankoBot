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
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.model.command.invoke
import io.ktor.client.request.*
import io.ktor.http.*

@Suppress("MagicNumber")
@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun hastebinCommand() = command("hastebin") {
    invoke(StringArgument.asSlashArgument("Der hochzuladende String")) {
        val hasteContent = message.content.substring(message.content.indexOf("hastebin") + 8, message.content.length)
        respond(HastebinUtil.postToHastebin(hasteContent))
    }
}

fun Kord.autoUploadListener() = on<MessageCreateEvent> {
    val file = message.attachments.firstOrNull() ?: return@on
    if (!file.isImage && file.filename == "message.txt") {
        message.channel.doExpensiveTask(makeEmbed()) {
            editEmbed(makeEmbed(autoUpload(message)))
        }
    }
}

private suspend fun autoUpload(message: Message): String {
    val attachment = message.attachments.firstOrNull()
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
    "Uff das ist aber viel",
    """soviel text gedacht.| Benutze am besten einen paste Dienst wie ${Config.HASTE_HOST}.
            | Hier ich mache das mal schnell für dich: $url
            |""".trimMargin()
)
