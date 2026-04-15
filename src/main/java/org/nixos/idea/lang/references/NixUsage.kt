package org.nixos.idea.lang.references

import com.intellij.find.usages.api.PsiUsage
import com.intellij.find.usages.api.ReadWriteUsage
import com.intellij.find.usages.api.UsageAccess
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import org.nixos.idea.psi.NixPsiElement

@Suppress("UnstableApiUsage")
internal class NixUsage private constructor(
    private val myIdentifier: NixPsiElement,
    private val myIsDeclaration: Boolean,
    private var myPointer: Pointer<NixUsage>? = null
) : PsiUsage, ReadWriteUsage {

    constructor(declaration: NixSymbolDeclaration) : this(declaration.identifier, true)

    constructor(reference: NixSymbolReference) : this(reference.identifier, false)

    override fun createPointer() = myPointer ?: Pointer.uroborosPointer<NixUsage, NixPsiElement>(
        SmartPointerManager.createPointer(myIdentifier)
    ) { identifier: NixPsiElement, pointer: Pointer<NixUsage> ->
        NixUsage(
            identifier,
            myIsDeclaration,
            pointer,
        )
    }.also {
        myPointer = it
    }

    override val file: PsiFile = myIdentifier.containingFile

    override val range: TextRange = myIdentifier.textRange

    // IDEA removes all instances which return true from the result of the usage search
    override val declaration = myIsDeclaration //: Boolean = !instance.showDeclarationsAsUsages && myIsDeclaration

    override fun computeAccess() = if (myIsDeclaration) UsageAccess.Write else UsageAccess.Read
}
