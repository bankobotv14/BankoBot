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

package de.nycode.bankobot.docdex

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

object DocDex {

    /**
     * Retrieves a list of all available javadocs.
     */
    @OptIn(ExperimentalTime::class)
    @Suppress("MagicNumber")
    suspend fun allJavadocs(): List<JavaDoc> = BankoBot.httpClient.get(Config.DOCDEX_URL) {
        url {
            path("javadocs")
        }
    }

    /**
     * Retrieves a list of [DocumentedElements][DocumentedElement] from the [javadoc].
     */
    suspend fun search(javadoc: String, query: String): List<DocumentedElement> =
        BankoBot.httpClient.get(Config.DOCDEX_URL) {
            url {
                path("index")
                parameter("javadoc", javadoc)
                parameter("query", query)
            }
        }
}

@Serializable
class JavaDoc(
    val names: List<String>,
    val link: String,
    @SerialName("actual_link")
    val actualLink: String
)
