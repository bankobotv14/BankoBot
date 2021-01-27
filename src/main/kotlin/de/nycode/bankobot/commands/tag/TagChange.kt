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

package de.nycode.bankobot.commands.tag

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class TagChange {
    abstract val field: String
}

@Serializable
data class AuthorChange(
    @Contextual
    val oldAuthor: Snowflake,
    @Contextual
    val newAuthor: Snowflake
) : TagChange() {
    override val field = "author"
}

@Serializable
data class NameChange(
    val oldName: String,
    val newName: String
) : TagChange() {
    override val field = "name"
}

@Serializable
data class TextChange(
    val oldText: String,
    val newText: String
) : TagChange() {
    override val field = "text"
}

@Serializable
data class AliasesChange(
    val oldAliases: List<String>,
    val newAliases: List<String>
) : TagChange() {
    override val field = "aliases"
}
