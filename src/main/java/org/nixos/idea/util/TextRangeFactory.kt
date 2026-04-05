package org.nixos.idea.util

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.nixos.idea.psi.NixPsiElement

/**
 * Factory methods to construct [TextRange] instances from PSI elements.
 */
object TextRangeFactory {
    /**
     * Creates [TextRange] for an element relative to itself.
     * 
     * @param element The element for which to create the range.
     * @return The range with an offset of zero and a length of the given element.
     */
    fun root(element: NixPsiElement) = TextRange.from(0, element.textLength)

    /**
     * Creates [TextRange] for an element relative to the given parent.
     * 
     * @param element The element for which to create the range.
     * @param parent  The parent element which becomes the reference frame for the returned range.
     * @return The range of the given `element` relative to the given `parent`.
     */
    @JvmStatic
    fun relative(element: NixPsiElement, parent: NixPsiElement): TextRange {
        assert(isChild(element, parent)) { "$element not a child of $parent" }
        val offset = element.node.startOffset - parent.node.startOffset
        return TextRange.from(offset, element.textLength)
    }

    private fun isChild(child: NixPsiElement, parent: NixPsiElement): Boolean {
        var current: PsiElement? = child
        while (current != null) {
            if (current === parent) {
                return true
            }
            current = current.parent
        }
        return false
    }
}
