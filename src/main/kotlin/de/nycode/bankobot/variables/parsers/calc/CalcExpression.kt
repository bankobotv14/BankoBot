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

package de.nycode.bankobot.variables.parsers.calc

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.variables.Expression
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class CalcExpression(val expression: String) : Expression<CalcExpressionResult> {

    private var result: CalcExpressionResult? = null

    companion object {
        @OptIn(KtorExperimentalAPI::class, ExperimentalTime::class)
        private val httpClient = HttpClient(CIO) {
            expectSuccess = false
            install(HttpTimeout) {
                requestTimeoutMillis = 10.seconds.toLongMilliseconds()
                connectTimeoutMillis = 10.seconds.toLongMilliseconds()
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
