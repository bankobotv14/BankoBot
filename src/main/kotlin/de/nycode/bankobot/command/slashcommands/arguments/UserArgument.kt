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
import dev.kord.core.KordObject
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.interaction.UserBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.kord.argument.MemberArgument
import dev.kord.x.commands.kord.argument.UserArgument

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see UserBuilder
 * @see UserArgument
 */
@JvmName("asUserSlashArgument")
@OptIn(KordPreview::class)
fun <T : User?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    builder: UserBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> = UserSlashArgument(description, this, builder)

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see UserBuilder
 * @see MemberArgument
 */
@JvmName("asMemberSlashArgument")
@OptIn(KordPreview::class)
fun <T : Member?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    builder: UserBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> = UserSlashArgument(description, this, builder)

@OptIn(KordPreview::class)
private class UserSlashArgument<T : KordObject?, CONTEXT>(
    description: String,
    delegate: Argument<T, CONTEXT>,
    private val builder: UserBuilder.() -> Unit,
) : AbstractSlashCommandArgument<T, CONTEXT>(description, delegate) {
    override fun BaseApplicationBuilder.applyArgument() {
        user(name, description, builder.applyRequired())
    }
}
