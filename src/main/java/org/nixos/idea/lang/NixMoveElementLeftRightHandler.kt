package org.nixos.idea.lang

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler
import com.intellij.psi.PsiElement
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixExprApp
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprLambda
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixExprList
import org.nixos.idea.psi.NixFormals
import org.nixos.idea.psi.NixPsiUtil

class NixMoveElementLeftRightHandler : MoveElementLeftRightHandler() {
    override fun getMovableSubElements(element: PsiElement): Array<PsiElement?> = when (element) {
        is NixExprList -> element.getItems().toTypedArray()

        is NixBindInherit -> element.getAttributes().toTypedArray()

        is NixExprAttrs -> element.getBindList().toTypedArray()

        is NixExprLet -> element.getBindList().toTypedArray()

        is NixExprLambda -> arrayOf(element.getArgument(), element.getFormals())

        is NixFormals -> element.getFormalList().toTypedArray()

        is NixExprApp -> NixPsiUtil.getArguments(element).toTypedArray()

        else -> PsiElement.EMPTY_ARRAY
    }
}
