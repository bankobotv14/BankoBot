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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic

val DocumentationModule = SerializersModule {
    polymorphic(DocumentedObject::class) {
        subclass(DocumentedClass::class, DocumentedClass.serializer())
        subclass(DocumentedEnum::class, DocumentedEnum.serializer())
        subclass(DocumentedInterface::class, DocumentedInterface.serializer())
        subclass(DocumentedAnnotation::class, DocumentedAnnotation.serializer())
        subclass(DocumentedMethod::class, DocumentedMethod.serializer())
        subclass(DocumentedConstructor::class, DocumentedConstructor.serializer())
    }

    contextual(DocumentedObject.serializer())
}

@Serializable
data class DocumentedElement(
    val name: String,
    val `object`: DocumentedObject,
)

@Serializable
sealed class DocumentedObject {
    abstract val link: String
    val type: Type
        get() = when (this) {
            is DocumentedClass -> Type.CLASS
            is DocumentedEnum -> Type.ENUM
            is DocumentedInterface -> Type.INTERFACE
            is DocumentedAnnotation -> Type.ANNOTATION
            is DocumentedConstructor -> Type.CONSTRUCTOR
            is DocumentedMethod -> Type.METHOD
            else -> error("Unknown type")
        }

    @Suppress("VariableNaming")
    abstract val `package`: String
    abstract val name: String
    abstract val description: String

    @SerialName("stripped_description")
    abstract val strippedDescription: String
    abstract val annotations: List<DocumentedReference>
    abstract val deprecated: Boolean

    @SerialName("deprecation_message")
    abstract val deprecationMessage: String
    abstract val modifiers: List<String>
    abstract val metadata: Metadata

    @Serializable
    enum class Type {
        CLASS,
        INTERFACE,
        ENUM,
        ANNOTATION,

        METHOD,
        CONSTRUCTOR,

        UNKNOWN
    }

    interface Metadata
}

abstract class DocumentedClassObject : DocumentedObject() {
    abstract override val metadata: ClassMetadata
}

abstract class DocumentedMethodObject : DocumentedObject() {
    abstract override val metadata: MethodMetadata
}

@SerialName("CLASS")
@Serializable
data class DocumentedClass(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: ClassMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message")
    override val deprecationMessage: String,
) : DocumentedClassObject()

@SerialName("ENUM")
@Serializable
data class DocumentedEnum(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: ClassMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message") override val deprecationMessage: String,
) : DocumentedClassObject()

@SerialName("INTERFACE")
@Serializable
data class DocumentedInterface(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: ClassMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message")
    override val deprecationMessage: String,
) : DocumentedClassObject()

@SerialName("ANNOTATION")
@Serializable
data class DocumentedAnnotation(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: ClassMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message")
    override val deprecationMessage: String,
) : DocumentedClassObject()

@SerialName("METHOD")
@Serializable
data class DocumentedMethod(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: MethodMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message")
    override val deprecationMessage: String,
) : DocumentedMethodObject()

@SerialName("CONSTRUCTOR")
@Serializable
data class DocumentedConstructor(
    override val link: String,
    override val description: String,
    @SerialName("stripped_description")
    override val strippedDescription: String,
    override val annotations: List<DocumentedReference>,
    override val modifiers: List<String>,
    override val metadata: MethodMetadata,
    override val `package`: String,
    override val name: String,
    override val deprecated: Boolean,
    @SerialName("deprecation_message")
    override val deprecationMessage: String,
) : DocumentedMethodObject()
