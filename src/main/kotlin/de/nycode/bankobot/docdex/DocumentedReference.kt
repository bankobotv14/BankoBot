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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = DocumentedReference.Companion::class)
sealed class DocumentedReference(val raw: String, val `package`: String, val className: String) {

    companion object : KSerializer<DocumentedReference> {
        private val packageRegex = "[a-z.0-9_]".toRegex()

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("reference", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: DocumentedReference) =
            encoder.encodeString(value.raw)

        override fun deserialize(decoder: Decoder): DocumentedReference {
            val input = decoder.decodeString().replace("@", "")
            val `package` = input.takeWhile { it.toString().matches(packageRegex) }

            // length = lastIndex + 1 => first char after the string
            val rest = input.substring(`package`.length)
            return when {
                rest.contains("#") -> {
                    val (className, methodName) = rest.split("#")
                    Method(input, `package`, className, methodName)
                }
                rest.contains("%") -> {
                    val (className, fieldName) = rest.split("%")
                    Field(input, `package`, className, fieldName)
                }
                else -> Class(input, `package`, rest)
            }
        }
    }

    class Class(raw: String, `package`: String, className: String) : DocumentedReference(
        raw, `package`,
        className
    )

    class Field(raw: String, `package`: String, className: String, val fieldName: String) :
        DocumentedReference(
            raw, `package`,
            className
        ) {

        @Suppress("MaxLineLength")
        override fun toString(): String {
            return "DocumentedReference.Field(raw='$raw', `package`='$`package`', className='$className', fieldName='$fieldName')"
        }
    }

    class Method(raw: String, `package`: String, className: String, val methodName: String) :
        DocumentedReference(
            raw, `package`,
            className
        ) {
        @Suppress("MaxLineLength")
        override fun toString(): String {
            return "DocumentedReference.Method(raw='$raw', `package`='$`package`', className='$className', methodName='$methodName')"
        }
    }

    override fun toString(): String {
        return "DocumentedReference(raw='$raw', `package`='$`package`', className='$className')"
    }
}
