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

package de.nycode.bankobot.command.slashcommands.arguments

import de.nycode.bankobot.command.slashcommands.SlashArgument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.extension.optional

/**
 * Abstract implementation for [SlashArgument] which delegates [Argument] to [delegate]
 * and already implements [required] based on [optional]
 */
abstract class AbstractSlashCommandArgument<T, CONTEXT>(
    override val description: String,
    private val delegate: Argument<T, CONTEXT>,
) : Argument<T, CONTEXT> by delegate, SlashArgument<T, CONTEXT> {
    override val required: Boolean
        get() = !delegate.toString().contains("optional")

    @OptIn(KordPreview::class)
    protected fun required(): OptionsBuilder.() -> Unit = {
        required = this@AbstractSlashCommandArgument.required
    }

    @OptIn(KordPreview::class)
    protected fun <T : OptionsBuilder> (T.() -> Unit).applyRequired(): T.() -> Unit {
        return {
            required()(this)
            invoke(this)
        }
    }
}
