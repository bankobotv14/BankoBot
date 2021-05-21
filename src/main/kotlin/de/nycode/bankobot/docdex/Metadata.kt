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
                encoder.encodeString("${annotations.joinToString(" ")} $type $name")
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
