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

package de.nycode.bankobot.config

import io.github.cdimascio.dotenv.dotenv
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Returns a delegated environment variable prefixed by [prefix] that
 * fallbacks to [default] if the found variable is empty or invalid
 */
fun getEnv(
    prefix: String? = null,
    default: String? = null
): EnvironmentVariable<String> =
    EnvironmentVariable(prefix, { it }, default)

/**
 * Returns a delegated environment variable prefixed by [prefix]
 * that fallbacks to [default] if the found variable is empty or invalid.
 *
 * The variable is transformed to [T] by [transform]
 */
fun <T> getEnv(
    prefix: String? = null,
    default: T? = null,
    transform: (String) -> T?
): EnvironmentVariable<T> =
    EnvironmentVariable(prefix, transform, default)

/**
 * Delegated property for a environment variable.
 *
 * @param prefix the prefix for the variable
 * @param transform a transformer to map the value to another type
 * @param default an optional default value
 *
 * @param T the type of the (transformed) variable
 *
 * @see getEnv
 * @see Config
 * @see ReadOnlyProperty
 */
@Suppress("LocalVariableName")
sealed class EnvironmentVariable<T>(
    private val prefix: String?,
    protected val transform: (String) -> T?,
    protected val default: T?,
) : ReadOnlyProperty<Any, T> {

    private val env = dotenv { ignoreIfMalformed = true; ignoreIfMissing = true }

    /**
     * Computes the name of the variable prefixed by [prefix].
     */
    protected val KProperty<*>.prefixedName: String
        get() = prefix?.let { it + name } ?: name

    /**
     * Makes this variable optional.
     *
     * @return a new [EnvironmentVariable] being optional
     */
    open fun optional(): EnvironmentVariable<T?> = Optional(prefix, transform, default)

    /**
     * Internal getter.
     */
    protected fun <T> getEnv(
        property: KProperty<*>,
        default: T? = null,
        transform: (String) -> T?
    ): T? = env[property.prefixedName]?.let(transform) ?: default

    private class Required<T>(prefix: String?, transform: (String) -> T?, default: T?) :
        EnvironmentVariable<T>(prefix, transform, default) {
        @Volatile
        private var _value: T? = null
        private fun missing(name: String): Nothing = error("Missing env variable: $name")

        @Suppress("UNCHECKED_CAST", "VariableNaming")
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val _v1 = _value
            if (_v1 != null) {
                return _v1
            }

            return synchronized(this) {
                val _v2 = _value
                if (_v2 != null) {
                    _v2
                } else {
                    val typedValue = getEnv(property, default, transform)
                    _value = typedValue
                    typedValue ?: missing(property.prefixedName)
                }
            }
        }
    }

    private class Optional<T>(prefix: String?, transform: (String) -> T?, default: T?) :
        EnvironmentVariable<T?>(prefix, transform, default) {
        private object UNINITIALIZED

        @Volatile
        private var _value: Any? = UNINITIALIZED

        override fun optional(): EnvironmentVariable<T?> = this

        @Suppress("UNCHECKED_CAST", "VariableNaming")
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val _v1 = _value
            if (_v1 != UNINITIALIZED) {
                return _v1 as T
            }

            return synchronized(this) {
                val _v2 = _value
                if (_v2 != UNINITIALIZED) {
                    _v2 as T
                } else {
                    val typedValue = getEnv(property, default, transform)
                    _value = typedValue
                    typedValue as T
                }
            }
        }
    }

    companion object {
        /**
         * @see EnvironmentVariable
         */
        operator fun <T> invoke(
            prefix: String?,
            transform: (String) -> T?,
            default: T?,
        ): EnvironmentVariable<T> = Required(prefix, transform, default)
    }
}
