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
import de.nycode.bankobot.command.callback
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.GitHubUtil
import dev.kord.common.annotation.KordPreview
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode

private const val GITHUB_BASE = "https://github.com/${GitHubUtil.GITHUB_REPO}"
private const val GITHUB_FILE_APPENDIX = "/tree/main/src/main/kotlin/"

@OptIn(KordPreview::class)
private val CommandArgument = WordArgument.named("command")
    .optional()
    .asSlashArgument("Der spezifische Befehl für den der Quellcode angezeigt werden soll")

@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun sourceCommand() = command("source") {
    alias("skid", "github")
    description("Zeigt dir den Sourcecode eines bestimmten Befehls")

    invoke(CommandArgument) { name ->
        if (name == null) {
            github()
        } else {
            specificCommand(name)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext") // Afaik there is no ASM based on coroutines
private suspend fun Context.specificCommand(name: String) {
    val command = processor.getCommand(name)
    if (command == null) {
        sendResponse(Embeds.error("Unbekannter Befehl", "Dieser Befehl ist nicht bekannt"))
        return
    }
    val (stack) = command.callback
    val reader = ClassReader(stack.className)
    val clazz = ClassNode(Opcodes.ASM9)
    reader.accept(clazz, Opcodes.ASM9)
    val method = clazz.methods.first { it.name == stack.methodName }
    val (start, end) = method.instructions
        .asSequence()
        .filterIsInstance<LineNumberNode>()
        .map(LineNumberNode::line)
        .toList()
    val url =
        "$GITHUB_BASE$GITHUB_FILE_APPENDIX${clazz.name.dropLast(2) /* Drop Kt file suffix */}.kt#L$start-L$end"

    sendResponse(
        Embeds.info(
            "Source code",
            "Den Code zu diesem Command findest du hier: [${stack.fileName}]($url)"
        )
    )
}

private suspend fun Context.github() = sendResponse(
    Embeds.info(
        "Source code",
        "Dieser Bot ist Open Source du findest ihn auf [GitHub]($GITHUB_BASE)"
    )
)
