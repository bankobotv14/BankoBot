/*
 *     This file is part of the BankoBot Project.
 *     Copyright (C) 2021  BankoBot Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <https://www.gnu.org/licenses/>.
 *
 */

package de.nycode.bankobot.docdex

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

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
