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

package de.nycode.bankobot.utils

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

object GoogleUtil {

    suspend fun getResults(search: String): Array<GoogleResult>? {
        val response: GoogleResponse =
            BankoBot.httpClient.get("https://www.googleapis.com/customsearch/v1") {
                parameter("q", search)
                parameter("key", Config.GOOGLE_API_KEY)
                parameter("cx", Config.GOOGLE_CX_CODE)
            }

        val items = response.items
        return if (response.kind == "customsearch#search") items
        else null
    }
}

@Serializable
class GoogleResponse(
    val kind: String,
    val items: Array<GoogleResult>
)

@Serializable
class GoogleResult(
    val kind: String,
    val title: String,
    val link: String,
    val snippet: String
)
