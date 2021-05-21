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
