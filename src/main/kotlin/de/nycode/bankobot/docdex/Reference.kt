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
