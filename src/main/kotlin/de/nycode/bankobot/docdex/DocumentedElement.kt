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
    val `object`: DocumentedObject
)

@Serializable
sealed class DocumentedObject {
    abstract val link: String
    val type: Type
        get() = when(this) {
            is DocumentedClass -> Type.CLASS
            is DocumentedEnum -> Type.ENUM
            is DocumentedInterface -> Type.INTERFACE
            is DocumentedAnnotation -> Type.ANNOTATION
            is DocumentedConstructor -> Type.CONSTRUCTOR
            is DocumentedMethod -> Type.METHOD
            else -> error("Unknown type")
        }
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
    override val deprecationMessage: String
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
    @SerialName("deprecation_message") override val deprecationMessage: String
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
    override val deprecationMessage: String
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
    override val deprecationMessage: String
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
    override val deprecationMessage: String
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
    override val deprecationMessage: String
) : DocumentedMethodObject()
