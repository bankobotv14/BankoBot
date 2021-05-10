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

package me.schlaubi.autohelp.source

import dev.kord.x.emoji.DiscordEmoji
import dev.schlaubi.forp.fetch.input.FileInput
import io.ktor.utils.io.*

/**
 * Representation of a received Discord message.
 */
public interface ReceivedMessage {

    /**
     * The id of the guild the message was received on.
     */
    public val guildId: Long

    /**
     * The id of the channel the message was received in.
     */
    public val channelId: Long

    /**
     * The id of the user authoring the message.
     */
    public val authorId: Long?

    /**
     * The content of the message.
     */
    public val content: String?

    /**
     * A list of files attached to the message.
     */
    public val files: List<ReceivedFile>

    /**
     * Adds [emoji] to the message.
     *
     * @see DiscordEmoji.unicode for non kord users
     */
    public suspend fun react(emoji: DiscordEmoji)

    /**
     * Representation of a file attachment.
     */
    public interface ReceivedFile {
        /**
         * The type of the file.
         *
         * @see FileInput.FileType
         */
        public val type: FileInput.FileType

        /**
         * Downloads the contents of the file and buffers it in a [ByteReadChannel].
         */
        public suspend fun download(): ByteReadChannel
    }
}
