package org.nixos.idea.psi

import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.psi.PsiElement
import org.nixos.idea.lang.references.NixSymbolDeclaration
import org.nixos.idea.lang.references.NixSymbolReference
import org.nixos.idea.lang.references.Scope

@Suppress("UnstableApiUsage")
interface NixPsiElement : PsiElement {
    val scope: Scope

    override fun getOwnDeclarations(): Collection<NixSymbolDeclaration>

    override fun getOwnReferences(): Collection<NixSymbolReference>

    fun <T> accept(visitor: NixElementVisitor<T?>): T?
}
