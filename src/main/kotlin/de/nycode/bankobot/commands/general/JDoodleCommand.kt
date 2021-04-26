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

package de.nycode.bankobot.commands.general

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.AbstractSlashCommandArgument
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.*
import de.nycode.bankobot.utils.Embeds.editEmbed
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.kord.argument.CodeBlock
import dev.kord.x.commands.kord.argument.CodeBlockArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.emoji.Emojis
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

private object JDoodleArgument :
    AbstractSlashCommandArgument<CodeBlock, MessageCreateEvent>(
        "Der Code der ausgeführt werden soll in einem CodeBlock",
        CodeBlockArgument.named("Code-Block")
    ) {
    @OptIn(KordPreview::class)
    override fun BaseApplicationBuilder.applyArgument() {
        string(name, description)
    }
}

@AutoWired
@ModuleName(GeneralModule)
fun jdoodleCommand() = command("jdoodle") {
    alias("jd", "execute", "exec")
    description("Kann Sachen ausführen.")

    invoke(JDoodleArgument) { argument ->
        executeViaJDoodle(argument)
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalTime::class)
private suspend fun Context.executeViaJDoodle(argument: CodeBlock) {
    doExpensiveTask("Loading JDoodle", "Bitte warte, bis ich deinen Code ausgeführt habe!") {
        val response = argument.language?.let { JDoodleUtil.executeCode(it, argument.content) }

        when (response?.statusCode) {
            null -> {
                val descriptionString = "JDoodle sagt nein, du hast leider keine Sprache angegeben."
                editEmbed(Embeds.error("Heute leider nicht!", descriptionString))
            }
            JDOODLE_LANGUAGE_INVALID_CODE -> {
                val descriptionString =
                    "JDoodle sagt nein, du hast leider keine unterstützte Sprache angegeben."
                editEmbed(Embeds.error("Heute leider nicht!", descriptionString))
            }
            JDOODLE_CREDITS_USED_INVALID_CODE -> {
                val descriptionString =
                    "JDoodle sagt nein, alle Requests sind für heute genutzt worden."
                editEmbed(Embeds.error("Heute leider nicht!", descriptionString))
            }
            else -> {
                val cpuTime = response.cpuTime?.seconds
                val memory = response.memory?.let { it / 1000.0 }
                val descriptionString = "```${Emojis.timerClock} $cpuTime " +
                        "${Emojis.desktopComputer} $memory KB```" +
                        "\n Output: \n```${response.output}```"

                editEmbed(EmbedBuilder()) {
                    title = "JDoodle Execution"
                    description = descriptionString
                }
            }
        }
    }
}
