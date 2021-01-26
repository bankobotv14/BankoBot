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
@file:Suppress("TopLevelPropertyNaming")

package de.nycode.bankobot.commands.tag

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
sealed class TagAction {
    @Contextual
    abstract val author: Snowflake

    @Contextual
    abstract val date: LocalDateTime
}

@Serializable
data class UseAction(
    @Contextual
    override val author: Snowflake,
    @Contextual
    override val date: LocalDateTime,
    @Contextual
    val tagId: Id<TagEntry>
) : TagAction()

@Serializable
class CreateAction(
    @Contextual
    override val author: Snowflake,
    @Contextual
    override val date: LocalDateTime,
    @Contextual
    val tagId: Id<TagEntry>,
    val name: String,
    val text: String
) : TagAction()

@Serializable
class DeleteAction(
    @Contextual
    override val author: Snowflake,
    @Contextual
    override val date: LocalDateTime,
    val name: String
) : TagAction()

@Serializable
class EditAction(
    @Contextual
    override val author: Snowflake,
    @Contextual
    override val date: LocalDateTime,
    val changes: List<TagChange>
) : TagAction()
