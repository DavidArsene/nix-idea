package org.nixos.idea.psi

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.nixos.idea.lang.NixLanguage

class NixTokenType(@NonNls debugName: @NonNls String) : IElementType(debugName, NixLanguage) {

    override fun toString() = if (this in NixTokenSets.KEYWORDS) {
        // The character U+2060 (Word Joiner) is used as a workaround to
        // make Grammar-Kit put quotation marks around keywords. See
        // https://github.com/JetBrains/Grammar-Kit/issues/262
        "\u2060" + super.toString()
    } else {
        super.toString()
    }
}
