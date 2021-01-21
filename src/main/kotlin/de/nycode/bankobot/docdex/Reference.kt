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

import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.result.ArgumentResult

// https://regex101.com/r/JvZoYD/4
private val classReferenceRegex = "(?:((?:(?:[a-zA-Z0-9]+)\\.?)+)\\.)?([a-zA-Z0-9]+)".toRegex()

// https://regex101.com/r/26jVyw/7
private val referenceRegex =
    "(?:((?:[a-zA-Z0-9]+\\.?)+)\\.)?([a-zA-Z0-9]+)[#.](<init>|[a-zA-Z0-9(), ]+)".toRegex()

/**
 * Argument that matches against a javadoc reference
 *
 * @see ReferenceArgument
 */
val ReferenceArgument: Argument<Reference, Any?> = InternalReferenceArgument("")

private class InternalReferenceArgument(override val name: String) :
    Argument<Reference, Any?> {

    @Suppress("MagicNumber")
    override suspend fun parse(
        text: String,
        fromIndex: Int,
        context: Any?
    ): ArgumentResult<Reference> {
        val inputRaw = text.substring(fromIndex)
        val input = inputRaw.trim()
        val classReference = classReferenceRegex.matchEntire(input)
        val diff = fromIndex + inputRaw.length - input.length

        return if (classReference != null) {
            val reference =
                Reference(classReference.value, classReference.groupValues[1], classReference.groupValues[2], null)
            ArgumentResult.Success(reference, classReference.value.length + diff)
        } else {
            val genericReference = referenceRegex.matchEntire(input)
            if (genericReference != null) {
                val reference = Reference(
                    genericReference.value,
                    genericReference.groupValues[1],
                    genericReference.groupValues[2],
                    genericReference.groupValues[3]
                )
                ArgumentResult.Success(reference, genericReference.value.length + diff)
            } else {
                ArgumentResult.Failure("Expected Java reference like List#add()", fromIndex)
            }
        }
    }
}

/**
 * Reference to a documented Java element (e.g. java.lang.String#substring(int, int)
 *
 * @property package the package of the reference
 * @property clazz the class name of the reference
 * @property method the reference name
 */
data class Reference(val raw: String, val `package`: String?, val clazz: String?, val method: String?) {

    /**
     * Converts this reference to a searchable DocDex query.
     */
    fun toDocDexQuery(): String {
        val query = StringBuilder()
        if (!`package`.isNullOrBlank()) {
            query.append(`package`).append('.')
        }
        if (!clazz.isNullOrBlank()) {
            query.append(clazz)
        }
        if (!method.isNullOrBlank()) {
            query.append("~").append(method)
        }
        return query.toString()
    }
}
