package org.nixos.idea.lang.references

import org.nixos.idea.psi.NixPsiElement

@Suppress("UnstableApiUsage")
class NixScopeReference(
    element: NixPsiElement, identifier: NixPsiElement, variableName: String
) :
    NixSymbolReference(element, identifier, variableName) {

    override fun resolveReference() = myElement.scope.resolveVariable(myName)
}
