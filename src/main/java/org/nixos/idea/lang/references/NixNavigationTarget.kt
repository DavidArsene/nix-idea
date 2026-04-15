package org.nixos.idea.lang.references

import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationRequest.Companion.sourceNavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.SmartPointerManager
import org.jetbrains.annotations.TestOnly
import org.nixos.idea.psi.NixPsiElement
import java.util.function.BiFunction

@Suppress("UnstableApiUsage")
class NixNavigationTarget(
    private val myIdentifier: NixPsiElement,
    private val myTargetPresentation: TargetPresentation,
    private var myPointer: Pointer<NavigationTarget?>? = null
) : NavigationTarget {

    @get:TestOnly
    val rangeInFile: TextRange?
        get() = myIdentifier.textRange

    override fun createPointer() = myPointer ?: Pointer.uroborosPointer<NavigationTarget?, NixPsiElement>(
        SmartPointerManager.createPointer(myIdentifier)
    ) { identifier: NixPsiElement, pointer: Pointer<NavigationTarget?>? ->
        NixNavigationTarget(
            identifier,
            myTargetPresentation,
            pointer,
        )
    }.also {
        myPointer = it
    }

    override fun computePresentation() = myTargetPresentation

    override fun navigationRequest() =
        sourceNavigationRequest(myIdentifier.containingFile, myIdentifier.textRange)
}
