package org.nixos.idea.psi

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.nixos.idea.lang.NixLanguage

class NixElementType(@NonNls debugName: @NonNls String) : IElementType(debugName, NixLanguage)
