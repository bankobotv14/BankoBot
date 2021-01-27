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

package de.nycode.bankobot.command.slashcommands

import de.nycode.bankobot.command.description
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.x.commands.model.command.AliasInfo
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.command.CommandEvent
import dev.kord.x.commands.model.processor.CommandProcessor

/**
 * Registers all registered command (except the ones using [disableSlashCommands]) to this [kord] instance.
 *
 * @see disableSlashCommands
 */
suspend fun CommandProcessor.registerSlashCommands(kord: Kord) {
    commands.values
        .asSequence()
        .filter { it.aliasInfo is AliasInfo.Parent }
        .filter { it.supportsSlashCommands }
        .forEach { kord.registerCommand(it) }
}

/**
 * Registers [command] as a Slash Command for this kord instance.
 */
@OptIn(KordPreview::class)
private suspend fun Kord.registerCommand(command: Command<out CommandEvent>) {
    slashCommands.createGlobalApplicationCommand(command.name,
        command.description ?: "<unknown description>") {
        command.arguments.forEach {
            if (it is SlashArgument<*, *>) {
                with(it) {
                    applyArgument()
                }
            } else error("Command ${command.name} does use incompatible arguments for slashCommands: $it")
        }
    }
}
