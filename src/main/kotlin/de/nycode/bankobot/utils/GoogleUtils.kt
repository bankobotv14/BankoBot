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

package de.nycode.bankobot.utils

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
