package org.nixos.idea.lang.highlighter

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlin.reflect.jvm.javaField


internal class NixHighlightVisitor : HighlightVisitor {
    private var myHolder: HighlightInfoHolder? = null
    private var myDelegate: Delegate? = null

    override fun suitableForFile(file: PsiFile) = NixHighlightVisitorDelegate.suitableForFile(file)

    override fun visit(element: PsiElement) = myDelegate!!.visit(element)

    override fun analyze(
        file: PsiFile,
        updateWholeFile: Boolean,
        holder: HighlightInfoHolder,
        action: Runnable
    ): Boolean {
        try {
            myHolder = holder
            myDelegate = Delegate()
            action.run()
        } finally {
            myHolder = null
            myDelegate = null
            // ::myHolder.javaField!!.set(this, null)
            // ::myDelegate.javaField!!.set(this, null)
        }
        return true
    }

    override fun clone() = NixHighlightVisitor()

    private inner class Delegate : NixHighlightVisitorDelegate() {
        override fun highlight(element: PsiElement, source: PsiElement?, attrPath: String, type: HighlightInfoType?) {
            if (type != null) {
                myHolder!!.add(
                    HighlightInfo.newHighlightInfo(type)
                        .range(element)
                        .create()
                )
            }
        }
    }
}
