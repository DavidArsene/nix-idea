package org.nixos.idea.lang.references

import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation.Companion.builder
import org.nixos.idea.lang.references.symbol.NixUserSymbol
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.util.TextRangeFactory.relative

@Suppress("UnstableApiUsage")
class NixSymbolDeclaration(
    private val myDeclarationElement: NixPsiElement,
    internal val identifier: NixPsiElement,
    private val mySymbol: NixUserSymbol,
    private val myDeclarationElementName: String,
    private val myDeclarationElementType: String?
) : PsiSymbolDeclaration {

    fun navigationTarget() = NixNavigationTarget(
        this.identifier, builder(mySymbol.presentation())
            .presentableText(myDeclarationElementName)
            .containerText(myDeclarationElementType)
            .presentation()
    )

    override fun getDeclaringElement() = myDeclarationElement

    override fun getRangeInDeclaringElement() = relative(this.identifier, myDeclarationElement)

    override fun getSymbol() = mySymbol
}
