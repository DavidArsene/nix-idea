package org.nixos.idea.lang

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.nixos.idea.psi.NixTypes

internal class NixBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        val openingBrace = file.findElementAt(openingBraceOffset)
        if (openingBrace?.node?.elementType !== NixTypes.LCURLY) {
            return openingBraceOffset
        }

        val previousToken = openingBrace.prevSibling
        return if (previousToken?.node?.elementType === NixTypes.DOLLAR) {
            openingBraceOffset - 1
        } else openingBraceOffset
    }
}

// Grammar-Kit uses the first pair of this array to guide the error recovery
// (even when structural is set to false). Since the lexer tracks curly
// braces for its state transitions, the curly braces must be on top to keep
// the state of parser and lexer consistent. See
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010379000
@JvmField
val PAIRS: Array<BracePair> = arrayOf(
    BracePair(NixTypes.LCURLY, NixTypes.RCURLY, true),
    BracePair(NixTypes.LBRAC, NixTypes.RBRAC, false),
    BracePair(NixTypes.LPAREN, NixTypes.RPAREN, false),
    BracePair(NixTypes.IND_STRING_OPEN, NixTypes.IND_STRING_CLOSE, false),
    BracePair(NixTypes.STRING_OPEN, NixTypes.STRING_CLOSE, false)
)
