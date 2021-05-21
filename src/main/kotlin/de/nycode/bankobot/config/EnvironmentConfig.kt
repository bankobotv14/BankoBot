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
