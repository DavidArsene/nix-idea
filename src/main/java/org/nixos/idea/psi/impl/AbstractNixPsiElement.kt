package org.nixos.idea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import org.nixos.idea.lang.references.NixScopeReference
import org.nixos.idea.lang.references.NixSymbolReference
import org.nixos.idea.lang.references.Scope
import org.nixos.idea.lang.references.Scope.Companion.root
import org.nixos.idea.lang.references.Scope.Companion.subScope
import org.nixos.idea.psi.NixAttr
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixExpr
import org.nixos.idea.psi.NixExprSelect
import org.nixos.idea.psi.NixExprVar
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.psi.NixPsiUtil

@Suppress("UnstableApiUsage")
abstract class AbstractNixPsiElement internal constructor(node: ASTNode) : ASTWrapperPsiElement(node), NixPsiElement {

//    override val scope: Scope = CachedValuesManager.getCachedValue<Scope>(this, KEY_SCOPE) {
//        val parentScope = (parent as? NixPsiElement)?.scope ?: root()
//        val result = subScope(parentScope, this)
//        CachedValueProvider.Result.create(result, this)
//    }

    // FIXME: does lazy work?
    override val scope: Scope by lazy {
        val parentScope = (parent as? NixPsiElement)?.scope ?: root()
        subScope(parentScope, this)
    }

    override fun getOwnDeclarations() = AbstractNixDeclarationHost.getDeclarations(this)

//    internal val declarationHost: AbstractNixDeclarationHost? = CachedValuesManager.getCachedValue(
//        this,
//        KEY_DECLARATION_HOST
//    ) {
//        val result = (this as? AbstractNixDeclarationHost)
//            ?: (parent as? AbstractNixPsiElement)?.declarationHost
//        CachedValueProvider.Result.create(result, this)
//    }

    internal val declarationHost: AbstractNixDeclarationHost? by lazy {
        (this as? AbstractNixDeclarationHost)
            ?: (parent as? AbstractNixPsiElement)?.declarationHost
    }

    override fun getOwnReferences(): Collection<NixSymbolReference> {
//        if (!instance.enabled) {
//            return mutableListOf()
//        }
        return when (this) {
            is NixExprVar -> listOf(NixScopeReference(this, this, text))

            // TODO: Attribute reference support
            is NixExprSelect -> mutableListOf()

            is NixBindInherit -> {
                val accessedObject: NixExpr? = this.getSource()
                if (accessedObject != null) {
                    // TODO: Attribute reference support
                    mutableListOf()
                } else {
                    this.getAttributes().flatMap { attr: NixAttr ->

                        val variableName = NixPsiUtil.getAttributeName(attr) ?: return listOf()
                        return listOf(NixScopeReference(this, attr, variableName))
                    }
                }
            }

            else -> mutableListOf()
        }
    }

//    companion object {
//        private val KEY_DECLARATION_HOST =
//            Key.create<CachedValue<AbstractNixDeclarationHost?>?>("AbstractNixPsiElement.declarationHost")
//        private val KEY_SCOPE = Key.create<CachedValue<Scope?>?>("AbstractNixPsiElement.scope")
//    }
}
