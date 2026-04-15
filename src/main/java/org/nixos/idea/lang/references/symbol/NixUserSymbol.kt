package org.nixos.idea.lang.references.symbol

import com.intellij.icons.AllIcons
import com.intellij.model.Pointer
import com.intellij.navigation.NavigatableSymbol
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.LocalSearchScope
import org.nixos.idea.lang.highlighter.NixTextAttributes
import org.nixos.idea.lang.references.NixSymbolDeclaration
import org.nixos.idea.lang.references.symbol.Commons.buildPresentation
import org.nixos.idea.psi.NixDeclarationHost
import java.lang.invoke.MethodHandles
import java.util.Objects
import javax.swing.Icon

@Suppress("UnstableApiUsage")
data class NixUserSymbol(
    val host: NixDeclarationHost, val path: List<String>, val type: Type
) : NixSymbol(), NavigatableSymbol {

    // Modified via MY_POINTER (VarHandle)
    private val pointer: Pointer<NixUserSymbol>? = null

    override val name = path.last()

    val declarations = host.getDeclarations(path)

    override fun createPointer(): Pointer<NixUserSymbol> {
        if (pointer == null) {
            MY_POINTER.compareAndSet(
                this, null, Pointer.uroborosPointer<NixUserSymbol?, NixDeclarationHost?>(
                    SmartPointerManager.createPointer(host)
                ) { host, pointer ->

                    host!!.getSymbol(path).also { symbol ->
                        if (symbol != null) {
                            MY_POINTER.compareAndSet(symbol, null, pointer!!)
                        }
                    }
                }
            )
            Objects.requireNonNull(pointer, "Pointer.uroborosPointer(...) must not return null")
        }
        return pointer!!
    }

    override fun presentation(): TargetPresentation {
        // TODO: TargetPresentationBuilder.locationText should specify the module (e.g. <nixpkgs>).
        //  See also PsiElementNavigationTarget.
        val file = host.containingFile
        return buildPresentation(name, type.icon, type.nameAttributes)
            .locationText(file?.name, file?.getIcon(0))
            .presentation()
    }

    override val maximalSearchScope = if (type.localScope != null) {
        assert(path.size == 1)
        LocalSearchScope(host, type.localScope)
    } else {
        super.maximalSearchScope
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        assert(host.project == project)

        val targets = host.getDeclarations(path)
            .map(NixSymbolDeclaration::navigationTarget)

        return if (false /* instance.jumpToFirstDeclaration */) {
            targets.take(1)
        } else targets
    }

    enum class Type(
        internal val icon: Icon,
        internal val nameAttributes: TextAttributesKey,
        internal val localScope: String?
    ) {
        ATTRIBUTE(AllIcons.Nodes.Property, NixTextAttributes.IDENTIFIER, null),
        PARAMETER(AllIcons.Nodes.Parameter, NixTextAttributes.PARAMETER, "Scope of Parameter"),
        VARIABLE(AllIcons.Nodes.Variable, NixTextAttributes.LOCAL_VARIABLE, "Scope of Variable"),
    }

    init {
        assert(!path.isEmpty())
    }

    companion object {
        // VarHandle mechanics
        private val MY_POINTER = MethodHandles.lookup()
            .findVarHandle(NixUserSymbol::class.java, "pointer", Pointer::class.java)
    }
}
