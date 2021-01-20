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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ClassMetadata(
    val extensions: List<DocumentedReference>,
    val implementations: List<DocumentedReference>,
    @SerialName("all_implementations")
    val allImplementations: List<DocumentedReference>,
    @SerialName("super_interfaces")
    val superInterfaces: List<DocumentedReference>,
    @SerialName("sub_interfaces")
    val subInterfaces: List<DocumentedReference>,
    @SerialName("sub_classes")
    val subClasses: List<DocumentedReference>,
    @SerialName("implementing_classes")
    val implementingClasses: List<DocumentedReference>,
    val fields: List<DocumentedReference>,
    val methods: List<DocumentedReference>
) : DocumentedObject.Metadata

@Serializable
data class MethodMetadata(
    val owner: String,
    val parameters: List<MethodParameter>,
    @SerialName("parameter_descriptions")
    val parameterDescriptions: Map<String, String>,
    val returns: String,
    @SerialName("returns_description")
    val returnsDescription: String,
    val throws: List<ThrowsInfo>
) : DocumentedObject.Metadata {
    @Serializable
    data class ThrowsInfo(
        @SerialName("key")
        val exception: String,
        @SerialName("value")
        val description: String
    )

    @Serializable(with = MethodParameter.Companion::class)
    data class MethodParameter(val annotations: List<String>, val type: String, val name: String) {
        companion object : KSerializer<MethodParameter> {
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("MethodParameter", PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: MethodParameter) = with(value) {
                encoder.encodeString("$type $name")
            }

            override fun deserialize(decoder: Decoder): MethodParameter {
                val input = decoder.decodeString().split(" |\\s".toRegex())
                return if (input.size == 2) {
                    val (type, name) = input
                    MethodParameter(emptyList(), type, name)
                } else {
                    val annotations = input.dropLast(2)
                    val (type, name) = input.takeLast(2)
                    MethodParameter(annotations, type, name)
                }
            }
        }
    }
}
