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

import de.nycode.bankobot.command.callback
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import de.nycode.bankobot.utils.GitHubUtil
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.kord.model.KordEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.invoke

private const val GITHUB_BASE = "https://github.com/${GitHubUtil.GITHUB_REPO}"
private const val GITHUB_FILE_APPENDIX = "/tree/main/src/main/kotlin/"

@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun sourceCommand() = command("source") {
    alias("skid", "github")
    description("Zeigt dir den Sourcecode eines bestimmten Befehls")

    invoke(WordArgument.named("command").optional()) { name ->
        if (name == null) {
            github()
        } else {
            specificCommand(name)
        }
    }
}

private suspend fun KordCommandEvent.specificCommand(name: String) {
    val command = processor.getCommand(name)
    if (command == null) {
        respondEmbed(Embeds.error("Unbekannter Befehl", "Dieser Befehl ist nicht bekannt"))
        return
    }
    val (stack, fileName) = command.callback
    val url = "$GITHUB_BASE$GITHUB_FILE_APPENDIX${fileName.replace(".", "/")}.kt#L${stack.lineNumber}"

    respondEmbed(
        Embeds.info(
            "Source code",
            "Den code zu diesem command findest du hier: [${stack.fileName}]($url)"
        )
    )
}

private suspend fun KordEvent.github() = respondEmbed(
    Embeds.info(
        "Source code",
        "Dieser Bot ist Open Source du findest ihn auf [GitHub](https://github.com/NyCodeGHG/BankoBot/)"
    )
)
