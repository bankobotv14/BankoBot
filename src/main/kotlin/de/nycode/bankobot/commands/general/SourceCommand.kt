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

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import de.nycode.bankobot.command.respond
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.GitHubUtil
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode

private const val GITHUB_BASE = "https://github.com/${GitHubUtil.GITHUB_REPO}"
private const val GITHUB_FILE_APPENDIX = "/tree/main/src/main/kotlin/"

class SourceArguments : Arguments() {
    val command by optionalString {
        name = "command"
        description = "Der spezifische Befehl für den der Quellcode angezeigt werden soll"
    }
}

suspend fun GeneralModule.sourceCommand() = ephemeralSlashCommand(::SourceArguments) {
    name = "source"
    description = "Zeigt dir den Sourcecode eines bestimmten Befehls"

    action {
        val name = arguments.command
        if (name == null) {
            github()
        } else {
            specificCommand(bot, name)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext") // Afaik there is no ASM based on coroutines
private suspend fun EphemeralInteractionContext.specificCommand(bot: ExtensibleBot, name: String) {

    val split = name.split("\\s+".toRegex())
    val commandName = split[0]
    val groupName = split.getOrNull(1)
    val groupCommandName = split.getOrNull(2)

    val command = bot.extensions
        .flatMap { (_, extension) -> extension.slashCommands }
        .firstOrNull { it.name == name }
        ?.let { parent ->
            when {
                groupCommandName != null -> {
                    val group = parent.groups[groupName]

                    group?.subCommands?.first { parent.name == groupCommandName } ?: parent
                }
                groupName != null -> parent.subCommands.firstOrNull { it.name == groupName } ?: parent
                else -> parent
            }
        }

    if (command == null) {
        respond(Embeds.error("Unbekannter Befehl", "Dieser Befehl ist nicht bekannt"))
        return
    }

    val reader = ClassReader(command.body::class.qualifiedName)
    val clazz = ClassNode(Opcodes.ASM9)
    reader.accept(clazz, Opcodes.ASM9)
    val method = clazz.methods.first { it.name == "stack.methodName" }
    val (start, end) = method.instructions
        .asSequence()
        .filterIsInstance<LineNumberNode>()
        .map(LineNumberNode::line)
        .toList()
    val url =
        "$GITHUB_BASE$GITHUB_FILE_APPENDIX${clazz.name.dropLast(2) /* Drop Kt file suffix */}.kt#L$start-L$end"

    respond(
        Embeds.info(
            "Source code",
            "Den Code zu diesem Command findest du hier: [${"stack.fileName"}]($url)"
        )
    )
}

private suspend fun EphemeralInteractionContext.github() = respond(
    Embeds.info(
        "Source code",
        "Dieser Bot ist Open Source du findest ihn auf [GitHub]($GITHUB_BASE)"
    )
)
