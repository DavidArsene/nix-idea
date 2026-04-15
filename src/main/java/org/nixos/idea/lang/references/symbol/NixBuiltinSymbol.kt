package org.nixos.idea.lang.references.symbol

import com.intellij.icons.AllIcons
import com.intellij.model.Pointer
import org.nixos.idea.lang.builtins.NixBuiltin
import org.nixos.idea.lang.highlighter.NixTextAttributes
import org.nixos.idea.lang.references.symbol.Commons.buildPresentation

@Suppress("UnstableApiUsage")
internal data class NixBuiltinSymbol(
    private val myBuiltin: NixBuiltin
) : NixSymbol(), Pointer<NixBuiltinSymbol> {

    override val name = myBuiltin.name

    override fun createPointer(): Pointer<NixBuiltinSymbol> = this

    override fun dereference(): NixBuiltinSymbol = this

    override fun presentation() =
        buildPresentation(myBuiltin.name, AllIcons.Nodes.Padlock, NixTextAttributes.BUILTIN)
            .presentation()
}
