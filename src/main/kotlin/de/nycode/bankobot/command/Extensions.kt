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

import de.nycode.bankobot.commands.general.sourceCommand
import dev.kord.x.commands.model.command.Command
import dev.kord.x.commands.model.context.CommonContext
import dev.kord.x.commands.model.metadata.Metadata
import dev.kord.x.commands.model.module.CommandSet

@PublishedApi
internal object CallbackData : Metadata.Key<CommandExecutionCallback>

data class CommandExecutionCallback(val stack: StackTraceElement)

@Suppress("UnsafeCallOnNullableType") // we know it's not null
val Command<*>.callback: CommandExecutionCallback
    get() = data.metadata[CallbackData]!!

/**
 * Replacement of [dev.kord.x.commands.model.module.command] which stores some data required for [sourceCommand]
 */
fun command(
    name: String,
    builder: CommandBuilder.() -> Unit,
): CommandSet {
    val stack = Exception().stackTrace[1]
    val configure: CommandBuilder.() -> Unit = {
        metaData[CallbackData] = CommandExecutionCallback(stack)
        builder(this)
    }
    return dev.kord.x.commands.model.module.command(CommonContext, name, configure)
}
