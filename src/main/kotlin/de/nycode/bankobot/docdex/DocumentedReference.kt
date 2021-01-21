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
        private val packageRegex = "[a-z.]".toRegex()

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
