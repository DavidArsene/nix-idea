package org.nixos.idea.psi

import org.nixos.idea.lang.references.NixSymbolDeclaration
import org.nixos.idea.lang.references.symbol.NixUserSymbol

/**
 * An element which may contain declarations.
 * There are two types of declaration hosts:
 *
 *  1. Elements which declare variables.
 * The declared variables are accessible in the subtree of this element.
 *
 *  * [NixExprLet]
 *  * [NixExprLambda]
 *  * [NixExprAttrs] if [recursive][NixPsiUtil.isRecursive]
 *
 *  1. Elements which declare attributes.
 * The attributes are accessible via the result of this expression.
 *
 *  * [NixExprAttrs] if not a [legacy let expression][NixPsiUtil.isLegacyLet]
 *
 *
 * These two cases are only implemented as one interface because
 * the implementation is effectively the same in case of [NixExprLet] and [NixExprAttrs].
 */
interface NixDeclarationHost : NixPsiElement {
    /**
     * Whether declarations of this element may be accessible as variables.
     * If this method returns `true`, [.getSymbolForScope] may be called to resolve a variable.
     *
     * @return `true` if the declarations shall be added to the scope.
     */
    val isDeclaringVariables: Boolean

    /**
     * Returns the symbol for the given variable name.
     * Must not be called when [.isDeclaringVariables] returns `false`.
     * Symbols exposed via this method become available from [.getScope] in all children.
     * The method returns `null` if no variable with the given name is declared from this element.
     *
     * @param variableName The name of the variable.
     * @return The symbol representing the variable, or `null`.
     */
    fun getSymbolForScope(variableName: String): NixUserSymbol?

    fun getSymbol(attributePath: List<String>): NixUserSymbol?

    fun getDeclarations(attributePath: List<String>): Collection<NixSymbolDeclaration>
}
