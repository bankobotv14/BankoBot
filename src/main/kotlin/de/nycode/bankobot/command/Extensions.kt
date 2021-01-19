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

package de.nycode.bankobot.command

import dev.kord.x.commands.kord.model.command.KordCommandBuilder
import dev.kord.x.commands.kord.model.processor.KordContext
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.metadata.Metadata
import dev.kord.x.commands.model.module.CommandSet
import java.util.*

@PublishedApi
internal object CallbackData : Metadata.Key<CommandExecutionCallback>

data class CommandExecutionCallback(val stack: StackTraceElement, val fileName: String)

val Command<*>.callback: CommandExecutionCallback
    get() = data.metadata[CallbackData]!!

fun command(
    name: String,
    builder: KordCommandBuilder.() -> Unit
): CommandSet {
    val stack = Exception().stackTrace[1]
    val fileName = builder.javaClass.name.substringBefore("Kt$")
    val configure: KordCommandBuilder.() -> Unit = {
        metaData[CallbackData] = CommandExecutionCallback(stack, fileName)
        builder(this)
    }
    return dev.kord.x.commands.model.module.command(KordContext, name, configure)
}