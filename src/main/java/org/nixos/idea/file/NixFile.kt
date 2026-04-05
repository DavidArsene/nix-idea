package org.nixos.idea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.nixos.idea.lang.NixLanguage
import javax.swing.Icon

class NixFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, NixLanguage) {
    override fun getFileType(): FileType = NixFileType

    override fun toString(): String = "Nix File"

    // override fun getIcon(flags: Int): Icon? = super.getIcon(flags)
}
