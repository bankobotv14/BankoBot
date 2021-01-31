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

import de.nycode.bankobot.command.MathExpressionArgument
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.AbstractSlashCommandArgument
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import de.nycode.bankobot.utils.HastebinUtil
import de.nycode.bankobot.variables.parsers.CalcExpression
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import net.dv8tion.jda.api.utils.MarkdownSanitizer

private object CalcArgument : AbstractSlashCommandArgument<CalcExpression, MessageCreateEvent>(
    "Der mathematische Ausdruck der ausgeführt werden soll",
    MathExpressionArgument
) {
    @OptIn(KordPreview::class)
    override fun BaseApplicationBuilder.applyArgument() {
        string(name, description, required())
    }
}

const val DISCORD_FIELD_MAX_LENGTH = 1024

@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun calcCommand(): CommandSet = command("calc") {
    alias("calculation", "calculate", "math")
    description("Führt eine mathematische Berechnung aus")

    invoke(CalcArgument) { expression ->
        var result = expression.getResult().toString()

        if (result.length > DISCORD_FIELD_MAX_LENGTH) {
            result = HastebinUtil.postToHastebin(result)
        }

        respondEmbed(Embeds.info("Mathematische Berechnung")) {
            field {
                name = "Berechnung"
                value = MarkdownSanitizer.escape(expression.input)
            }
            field {
                name = "Ergebnis"
                value = MarkdownSanitizer.escape(result)
            }
        }
    }
}
