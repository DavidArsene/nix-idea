package org.nixos.idea.lang.references

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.Contract
import org.nixos.idea.lang.builtins.NixBuiltin.Companion.resolveGlobal
import org.nixos.idea.lang.references.symbol.NixSymbol
import org.nixos.idea.lang.references.symbol.toSymbol
import org.nixos.idea.psi.NixDeclarationHost
import org.nixos.idea.psi.NixExprWith
import org.nixos.idea.psi.NixPsiElement

/**
 * Scope used to lookup accessible variables.
 * This class is immutable, but it stores references to PSI elements.
 * If the underlying PSI elements change after the scope instance was created,
 * the scope instance may or may not reflect the changes.
 */
abstract class Scope {
    //region Public API

    /**
     * Resolves the given variable name and returns matching symbols.
     *
     * @param variableName The name of the variable.
     * @return Symbols which may be references by the given variable.
     */
    @Contract(pure = true)
    fun resolveVariable(variableName: String): List<NixSymbol?> {
        val result = resolveVariable0(variableName)
        return ContainerUtil.createMaybeSingletonList<NixSymbol?>(result)
    }

    //endregion
    //region Abstract methods

    @Contract(pure = true)
    abstract fun resolveVariable0(variableName: String): NixSymbol?

    //endregion
    //region Subclasses

    /**
     * Represents the scope at the root of a file.
     * The scope only contains built-ins provided by Nix itself.
     * The same instance may be reused for multiple files.
     */
    private object Root : Scope() {
        override fun resolveVariable0(variableName: String): NixSymbol? {
            // TODO: Ideally, we should filter the built-ins based on the used version of Nix.
            val builtin = resolveGlobal(variableName)
            return builtin?.toSymbol()
        }
    }

    private abstract class Psi<T : NixPsiElement?>(
        /**
         * Returns the parent scope.
         *
         * @return Parent scope.
         */
        val parent: Scope,
        /**
         * Returns the element at the root of the scope.
         *
         * @return Root of the scope.
         */
        val element: T
    ) : Scope()

    private class LetOrRecursiveSet(parent: Scope, element: NixDeclarationHost) :
        Psi<NixDeclarationHost?>(parent, element) {
        override fun resolveVariable0(variableName: String): NixSymbol? {
            val symbol: NixSymbol? = this.element!!.getSymbolForScope(variableName)
            return symbol ?: this.parent.resolveVariable0(variableName)
        }
    }

    private class With(parent: Scope, element: NixExprWith) : Psi<NixExprWith>(parent, element) {
        // TODO with-expression reference support
        override fun resolveVariable0(variableName: String) =
            this.parent.resolveVariable0(variableName)
    }

    //endregion
    //region Factory methods

    companion object {
        @JvmStatic
        fun root(): Scope = Root

        /**
         * Use [NixPsiElement.getScope] instead of calling this method directly.
         */
        @JvmStatic
        fun subScope(parent: Scope, element: NixPsiElement) = when (element) {

            is NixExprWith -> With(parent, element)

            is NixDeclarationHost if element.isDeclaringVariables -> LetOrRecursiveSet(parent, element)

            else -> parent
        }
    }
}
