package org.nixos.idea.lang.highlighter

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.HighlightInfoType.HighlightInfoTypeImpl
import com.intellij.codeInsight.daemon.impl.HighlightInfoType.SYMBOL_TYPE_SEVERITY
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.nixos.idea.file.NixFile
import org.nixos.idea.lang.builtins.NixBuiltin
import org.nixos.idea.lang.builtins.NixBuiltin.Companion.resolveBuiltin
import org.nixos.idea.lang.builtins.NixBuiltin.Companion.resolveGlobal
import org.nixos.idea.psi.NixBind
import org.nixos.idea.psi.NixBindAttr
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprLambda
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixExprSelect
import org.nixos.idea.psi.NixExprVar
import org.nixos.idea.psi.NixIdentifier
import org.nixos.idea.psi.NixPsiUtil
import org.nixos.idea.psi.NixStdAttr
import java.util.function.BiPredicate

/**
 * Delegate for the highlighting logic used by [NixHighlightVisitor] and [NixRainbowVisitor].
 */
internal abstract class NixHighlightVisitorDelegate {
    /**
     * Callback which gets called for usages of variables and their attributes in the given file.
     *
     * @param element  The element which refers to a variable or attribute.
     * @param source   The element which defines the corresponding variable. Is `null` if the source cannot be determined.
     * @param attrPath The used attribute path.
     * @param type     The type of variable, i.e. [.LOCAL_VARIABLE] or [.PARAMETER].
     */
    abstract fun highlight(element: PsiElement, source: PsiElement?, attrPath: String, type: HighlightInfoType?)

    fun visit(element: PsiElement) {
        when {

            element is NixExprSelect -> {
                val value = element.value
                val attrPath = element.attrPath
                if (attrPath != null && value is NixExprVar) {
                    val identifier = value.text
                    val source: PsiElement? = findSource(element, identifier)
                    var pathStr = identifier
                    for (nixAttr in element.attrPath!!.attrList) {
                        if (nixAttr !is NixStdAttr) {
                            break
                        }
                        pathStr = pathStr + '.' + nixAttr.text
                        highlight(nixAttr, source, pathStr)
                    }
                }
            }

            // FIXME: wtf
            element is NixExprVar || ((element as? NixStdAttr)?.parent as? NixBindInherit)?.source == null -> {

                val identifier = element.text
                val source: PsiElement? = findSource(element, identifier)
                highlight(element, source, identifier)
            }

            else -> iterateVariables(element, true) { `var`: PsiElement?, path: String? ->
                highlight(`var`!!, element, path!!)
                false
            }
        }
    }

    private fun highlight(element: PsiElement, source: PsiElement?, attrPath: String) {
        var type: HighlightInfoType? = null
        if (!attrPath.contains(".")) {
            if (source != null) {
                type = getHighlightingBySource(source)
            } else {
                val builtin = resolveGlobal(attrPath)
                if (builtin != null) {
                    type = getHighlightingByBuiltin(builtin)
                }
            }
        } else if (attrPath.startsWith(BUILTINS_PREFIX)) {
            val builtin = resolveBuiltin(attrPath.substring(BUILTINS_PREFIX.length))
            if (builtin != null) {
                type = getHighlightingByBuiltin(builtin)
            }
        }
        highlight(element, source, attrPath, type)
    }

    companion object {
        private const val BUILTINS_PREFIX = "builtins."

        val LITERAL = HighlightInfoTypeImpl(SYMBOL_TYPE_SEVERITY, NixTextAttributes.LITERAL)
        val IMPORT = HighlightInfoTypeImpl(SYMBOL_TYPE_SEVERITY, NixTextAttributes.IMPORT)
        val BUILTIN = HighlightInfoTypeImpl(SYMBOL_TYPE_SEVERITY, NixTextAttributes.BUILTIN)
        val LOCAL_VARIABLE = HighlightInfoTypeImpl(SYMBOL_TYPE_SEVERITY, NixTextAttributes.LOCAL_VARIABLE)
        val PARAMETER = HighlightInfoTypeImpl(SYMBOL_TYPE_SEVERITY, NixTextAttributes.PARAMETER)

        fun suitableForFile(file: PsiFile) = file is NixFile

        private fun findSource(context: PsiElement, identifier: String): PsiElement? {
            var context = context
            while (true) {
                if (iterateVariables(
                        context,
                        false
                    ) { `var`: PsiElement?, _: String? -> `var`!!.textMatches(identifier) }
                ) return context

                context = context.parent ?: return null
            }
        }

        private fun iterateVariables(
            element: PsiElement,
            fullPath: Boolean,
            action: BiPredicate<PsiElement?, String?>
        ): Boolean {
            return when (element) {
                is NixExprLet -> iterateVariables(element.bindList, fullPath, action)

                is NixExprAttrs ->
                    NixPsiUtil.isRecursive(element) &&
                            iterateVariables(element.bindList, fullPath, action)

                is NixExprLambda ->
                    NixPsiUtil.getParameters(element).any { parameter ->
                        val identifier: NixIdentifier = parameter.identifier
                        return action.test(identifier, if (fullPath) identifier.text else null)
                    }

                else -> false
            }
        }

        private fun iterateVariables(
            bindList: MutableList<NixBind>,
            fullPath: Boolean,
            action: BiPredicate<PsiElement?, String?>
        ): Boolean {
            for (bind in bindList) {
                if (bind is NixBindAttr) {
                    if (fullPath) {
                        val attrs = bind.attrPath.attrList
                        val first = attrs[0]
                        if (first !is NixStdAttr) {
                            continue
                        }
                        var pathStr = first.text
                        if (action.test(first, pathStr)) {
                            return true
                        }
                        for (attr in attrs.subList(1, attrs.size)) {
                            if (attr !is NixStdAttr) {
                                break
                            }
                            pathStr = pathStr + '.' + attr.text
                            if (action.test(attr, pathStr)) {
                                return true
                            }
                        }
                    } else if (action.test(bind.attrPath.firstAttr, null)) {
                        return true
                    }
                } else if (bind is NixBindInherit) {
                    // `let { inherit x; } in ...` does not actually introduce a new variable
                    if (bind.getSource() != null) {
                        for (attr in bind.attributes) {
                            if (attr is NixStdAttr && action.test(attr, if (fullPath) attr.text else null)) {
                                return true
                            }
                        }
                    }
                } else {
                    throw IllegalStateException("Unexpected NixBind implementation: " + bind.javaClass)
                }
            }
            return false
        }

        private fun getHighlightingBySource(source: PsiElement) = when (source) {
            is NixExprLet, is NixExprAttrs -> LOCAL_VARIABLE
            is NixExprLambda -> PARAMETER
            else -> throw IllegalArgumentException("Invalid source: $source")
        }

        private fun getHighlightingByBuiltin(builtin: NixBuiltin) = when (builtin.highlightingType) {
            NixBuiltin.HighlightingType.IMPORT -> IMPORT
            NixBuiltin.HighlightingType.LITERAL -> LITERAL
            NixBuiltin.HighlightingType.OTHER -> BUILTIN
        }
    }
}
