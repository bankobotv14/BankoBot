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

package de.nycode.bankobot.variables.parsers.calc

import de.nycode.bankobot.config.Config
import de.nycode.bankobot.variables.Expression
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class CalcExpression(val expression: String) : Expression<CalcExpressionResult> {

    private var result: CalcExpressionResult? = null

    companion object {
        @OptIn(KtorExperimentalAPI::class, ExperimentalTime::class)
        private val httpClient = HttpClient(CIO) {
            expectSuccess = false
            install(HttpTimeout) {
                requestTimeoutMillis = 10.seconds.inWholeMilliseconds
                connectTimeoutMillis = 10.seconds.inWholeMilliseconds
            }
        }
    }

    override suspend fun getResult(): CalcExpressionResult {
        if (result == null) {
            val response = httpClient.get<HttpResponse>(Config.MATHJS_SERVER_URL) {
                url {
                    parameter("expr", expression)
                }
            }
            result = when (response.status) {
                HttpStatusCode.OK -> {
                    CalcExpressionResult(response.readText())
                }
                else -> {
                    CalcExpressionResult(response.readText(), true)
                }
            }
        }
        return result ?: CalcExpressionResult("Unable to reach expression evaluating service!", true)
    }
}

data class CalcExpressionResult(val result: String, val isError: Boolean = false)
