package org.nixos.idea.lang.highlighter

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

internal class NixRainbowVisitor : RainbowVisitor() {
    private var myDelegate: Delegate? = null

    override fun suitableForFile(file: PsiFile) = NixHighlightVisitorDelegate.suitableForFile(file)

    override fun visit(element: PsiElement) {
        myDelegate!!.visit(element)
    }

    override fun analyze(
        file: PsiFile,
        updateWholeFile: Boolean,
        holder: HighlightInfoHolder,
        action: Runnable
    ): Boolean {
        myDelegate = Delegate(file)
        try {
            return super.analyze(file, updateWholeFile, holder, action)
        } finally {
            myDelegate = null
        }
    }

    override fun clone(): HighlightVisitor = NixRainbowVisitor()

    private inner class Delegate(private val file: PsiFile) : NixHighlightVisitorDelegate() {

        override fun highlight(element: PsiElement, source: PsiElement?, attrPath: String, type: HighlightInfoType?) {

            if (type !== LITERAL && type !== IMPORT && type !== BUILTIN) {
                val attributesKey = type?.attributesKey ?: NixTextAttributes.IDENTIFIER
                val context = source ?: file

                addInfo(getInfo(context, element, attrPath, attributesKey))
            }
        }
    }

}

@JvmField
val RAINBOW_ATTRIBUTES: List<TextAttributesKey> = listOf(
    NixTextAttributes.LOCAL_VARIABLE,
    NixTextAttributes.PARAMETER
)
