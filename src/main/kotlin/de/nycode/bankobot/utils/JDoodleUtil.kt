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
import io.ktor.http.*
import kotlinx.serialization.Serializable

object JDoodleUtil {

    suspend fun executeCode(language: String, code: String): JDoodleResponse {
        val responseCredits: JDoodleCreditsUsed = BankoBot.httpClient.post("https://api.jdoodle.com/v1/credit-spent") {
            contentType(ContentType.Application.Json)
            body = JDoodleCreditsUsedRequest(Config.JDOODLE_ID, Config.JDOODLE_SECRET)
        }

        if (responseCredits.used >= Config.JDOODLE_REQUESTS_MAX) return JDoodleResponse(
            JDOODLE_CREDITS_USED_INVALID_CODE)

        if (JDoodleLanguageProvider.listOfLanguages.firstOrNull { jDoodleLanguage ->
                jDoodleLanguage.name == language
            } == null) return JDoodleResponse(JDOODLE_LANGUAGE_INVALID_CODE)

        val jdoodleLanguage =
            JDoodleLanguageProvider.listOfLanguages.firstOrNull { jDoodleLanguage -> jDoodleLanguage.name == language }

        return BankoBot.httpClient.post("https://api.jdoodle.com/v1/execute") {
            contentType(ContentType.Application.Json)
            if (jdoodleLanguage != null) {
                body = JDoodleRequest(
                    Config.JDOODLE_ID,
                    Config.JDOODLE_SECRET,
                    code,
                    language,
                    jdoodleLanguage.defaultVersion
                )
            }
        }
    }
}

@Serializable
class JDoodleCreditsUsed(
    val used: Int = 200,
    val error: String? = null,
    // Should be there for logging
    val statusCode: Int? = null
)

@Serializable
class JDoodleResponse(
    val statusCode: Int,
    val output: String? = null,
    val memory: Int? = null,
    val cpuTime: Double? = null,
    val error: String? = null
)

data class JDoodleLanguage(
    val name: String,
    val versions: IntRange,
    val defaultVersion: Int = versions.last
)

@Serializable
data class JDoodleRequest(
    val clientId: String,
    val clientSecret: String,
    val script: String,
    val language: String,
    val versionIndex: Int
)

@Serializable
data class JDoodleCreditsUsedRequest(
    val clientId: String,
    val clientSecret: String
)

@Suppress("MagicNumber")
object JDoodleLanguageProvider {
    private val VERSIONS_1 = (0..0)
    private val VERSIONS_2 = (0..1)
    private val VERSIONS_3 = (0..2)
    private val VERSIONS_4 = (0..3)
    private val VERSIONS_5 = (0..4)

    val listOfLanguages = arrayOf(
        JDoodleLanguage("kotlin", VERSIONS_3),
        JDoodleLanguage("java", VERSIONS_4),
        JDoodleLanguage("c", VERSIONS_5),
        JDoodleLanguage("c99", VERSIONS_4),
        JDoodleLanguage("cpp", VERSIONS_5),
        JDoodleLanguage("cpp14", VERSIONS_4),
        JDoodleLanguage("cpp17", VERSIONS_1),
        JDoodleLanguage("php", VERSIONS_4),
        JDoodleLanguage("perl", VERSIONS_4),
        JDoodleLanguage("python2", VERSIONS_3),
        JDoodleLanguage("python3", VERSIONS_4),
        JDoodleLanguage("ruby", VERSIONS_4),
        JDoodleLanguage("go", VERSIONS_4),
        JDoodleLanguage("scala", VERSIONS_4),
        JDoodleLanguage("bash", VERSIONS_4),
        JDoodleLanguage("sql", VERSIONS_4),
        JDoodleLanguage("pascal", VERSIONS_3),
        JDoodleLanguage("csharp", VERSIONS_4),
        JDoodleLanguage("haskell", VERSIONS_4),
        JDoodleLanguage("objc", VERSIONS_4),
        JDoodleLanguage("swift", VERSIONS_4),
        JDoodleLanguage("groovy", VERSIONS_4),
        JDoodleLanguage("fortran", VERSIONS_4),
        JDoodleLanguage("brainfuck", VERSIONS_1),
        JDoodleLanguage("lua", VERSIONS_3),
        JDoodleLanguage("tcl", VERSIONS_4),
        JDoodleLanguage("hack", VERSIONS_1),
        JDoodleLanguage("rust", VERSIONS_4),
        JDoodleLanguage("d", VERSIONS_2),
        JDoodleLanguage("r", VERSIONS_4),
        JDoodleLanguage("ada", VERSIONS_4),
        JDoodleLanguage("freebasic", VERSIONS_2),
        JDoodleLanguage("ada", VERSIONS_4),
        JDoodleLanguage("verilog", VERSIONS_3),
        JDoodleLanguage("cobol", VERSIONS_3),
        JDoodleLanguage("dart", VERSIONS_4),
        JDoodleLanguage("yabasic", VERSIONS_2),
        JDoodleLanguage("clojure", VERSIONS_3),
        JDoodleLanguage("nodejs", VERSIONS_4),
        JDoodleLanguage("scheme", VERSIONS_3),
        JDoodleLanguage("forth", VERSIONS_1),
        JDoodleLanguage("prolog", VERSIONS_2),
        JDoodleLanguage("octave", VERSIONS_4),
        JDoodleLanguage("coffeescript", VERSIONS_4),
        JDoodleLanguage("icon", VERSIONS_2),
        JDoodleLanguage("fsharp", VERSIONS_2),
        JDoodleLanguage("nasm", VERSIONS_4),
        JDoodleLanguage("gccasm", VERSIONS_3),
        JDoodleLanguage("intercal", VERSIONS_1),
        JDoodleLanguage("nasm", VERSIONS_4),
        JDoodleLanguage("nemerle", VERSIONS_1),
        JDoodleLanguage("ocaml", VERSIONS_2),
        JDoodleLanguage("unlambda", VERSIONS_1),
        JDoodleLanguage("picolisp", VERSIONS_4),
        JDoodleLanguage("spidermonkey", VERSIONS_2),
        JDoodleLanguage("rhino", VERSIONS_2),
        JDoodleLanguage("bc", VERSIONS_2),
        JDoodleLanguage("clisp", VERSIONS_4),
        JDoodleLanguage("elixir", VERSIONS_4),
        JDoodleLanguage("factor", VERSIONS_4),
        JDoodleLanguage("falcon", VERSIONS_1),
        JDoodleLanguage("fantom", VERSIONS_1),
        JDoodleLanguage("nim", VERSIONS_3),
        JDoodleLanguage("pike", VERSIONS_2),
        JDoodleLanguage("smalltalk", VERSIONS_1),
        JDoodleLanguage("mozart", VERSIONS_1),
        JDoodleLanguage("lolcode", VERSIONS_1),
        JDoodleLanguage("racket", VERSIONS_3),
        JDoodleLanguage("whitespace", VERSIONS_1),
        JDoodleLanguage("erlang", VERSIONS_1),
        JDoodleLanguage("jlang", VERSIONS_1)
    )
}
