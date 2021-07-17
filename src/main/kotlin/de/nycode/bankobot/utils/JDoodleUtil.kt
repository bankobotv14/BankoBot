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

    val listOfLanguages: Array<JDoodleLanguage> = arrayOf(
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
