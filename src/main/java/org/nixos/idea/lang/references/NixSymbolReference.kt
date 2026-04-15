package org.nixos.idea.lang.references

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.nixos.idea.lang.references.symbol.NixSymbol
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.util.TextRangeFactory.relative

@Suppress("UnstableApiUsage")
abstract class NixSymbolReference protected constructor(
    protected val myElement: NixPsiElement,
    val identifier: NixPsiElement,
    protected val myName: String
) : PsiSymbolReference {
    override fun getElement() = myElement

    override fun getRangeInElement() = relative(this.identifier, myElement)

    // Check name as a shortcut to avoid resolving the reference when it cannot match anyway.
    override fun resolvesTo(target: Symbol) =
        (target is NixSymbol && myName == target.name) &&
                super.resolvesTo(target)
}
