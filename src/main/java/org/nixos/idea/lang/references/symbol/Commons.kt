package org.nixos.idea.lang.references.symbol

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.platform.backend.presentation.TargetPresentation.Companion.builder
import com.intellij.platform.backend.presentation.TargetPresentationBuilder
import javax.swing.Icon

internal object Commons {
    @JvmStatic
    fun buildPresentation(name: String, icon: Icon, textAttributesKey: TextAttributesKey): TargetPresentationBuilder {
        val colorsScheme = EditorColorsManager.getInstance().schemeForCurrentUITheme
        return builder(name)
            .icon(icon)
            .presentableTextAttributes(colorsScheme.getAttributes(textAttributesKey))
    }
}
