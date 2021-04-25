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

package de.nycode.bankobot.utils

import dev.kord.x.commands.model.module.ModuleBuilder
import dev.kord.x.commands.model.module.ModuleModifier
import dev.kord.x.commands.model.processor.ModuleContainer
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

inline fun afterAll(
    crossinline modification: suspend Collection<ModuleBuilder<*, *, *>>.() -> Unit
): ModuleModifier = object : ModuleModifier {
    @Suppress("UNCHECKED_CAST")
    override suspend fun apply(container: ModuleContainer) {
        val properties =
            container::class.declaredMemberProperties.first { it.name == "modules" } as KProperty1<ModuleContainer, MutableMap<String, ModuleBuilder<*, *, *>>>

        val modules = properties.get(container)

        modification(modules.values)
    }
}
