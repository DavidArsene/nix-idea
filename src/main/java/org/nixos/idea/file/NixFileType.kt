package org.nixos.idea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import org.nixos.idea.icon.NixIcons
import org.nixos.idea.lang.NixLanguage
import javax.swing.Icon

object NixFileType : LanguageFileType(NixLanguage) {

    override fun getName(): String = "Nix"

    override fun getDescription(): String = "Nix language"

    override fun getDefaultExtension(): String = "nix"

    override fun getIcon(): Icon? = NixIcons.FILE
}
