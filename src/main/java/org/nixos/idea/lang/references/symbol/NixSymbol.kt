package org.nixos.idea.lang.references.symbol

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import org.jetbrains.annotations.Contract
import org.nixos.idea.lang.builtins.NixBuiltin

@Suppress("UnstableApiUsage")
abstract class NixSymbol internal constructor() : Symbol, SearchTarget {
    abstract val name: String

    abstract override fun createPointer(): Pointer<out NixSymbol?>

    override val usageHandler = UsageHandler.createEmptyUsageHandler(this.name)
}

// FIXME: was "NixSymbol.builtin"
internal fun NixBuiltin.toSymbol(): NixSymbol = NixBuiltinSymbol(this)
