package org.nixos.idea.lang

import com.intellij.codeInsight.editorActions.MultiCharQuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import org.nixos.idea.psi.NixTokenSets
import org.nixos.idea.psi.NixTypes

/**
 * Quote handler for the Nix Language.
 * This class handles the automatic insertion of closing quotes after the user enters opening quotes.
 * The methods are called in the following order whenever the user enters a quote character.
 *
 *  1. [.isClosingQuote] is only called if the typed quote character already exists just behind the caret.
 * If the method returns `true`, the insertion of the quote and all further methods will be skipped.
 * The caret will just move over the existing quote one character to the right.
 *  1. **Insert quote character.**
 *  1. Handling of [MultiCharQuoteHandler]:
 *  1. [.getClosingQuote] is called with the offset behind the inserted quote.
 * The returned value represents the string which shall be inserted.
 * Can return `null` to skipp further processing of `MultiCharQuoteHandler`.
 *  1. [.hasNonClosedLiteral] is called with the offset before the inserted quote.
 * The closing quotes returned by the previous method will only be inserted if this method returns `true`.
 *  1. **Insert closing quotes as returned by `getClosingQuote(...)`.**
 *
 *  1. Standard handling of [QuoteHandler] (skipped if closing quotes were already inserted):
 *  1. [.isOpeningQuote] is called with the offset before the inserted quote.
 * The following steps will only be executed if this method returns `true`.
 *  1. [.hasNonClosedLiteral] is called with the offset before the inserted quote.
 * The following steps will only be executed if this method returns `true`.
 *  1. **Insert same quote character as initially typed by the user again.**
 *
 *
 *
 * @see [Quote Handling Documentation](https://plugins.jetbrains.com/docs/intellij/additional-minor-features.html.quote-handling)
 */
internal class NixQuoteHandler : MultiCharQuoteHandler {
    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int) =
        iterator.tokenType in NixTokenSets.CLOSING_QUOTES

    // This method comes from QuoteHandler and assumes the quote is only one char in size.
    // We therefore ignore indented strings ('') in this method.
    // Note that this method is not actually used for the insertion of closing quotes,
    // as that is already handled by MultiCharQuoteHandler.getClosingQuote(...). See class documentation.
    // However, this method is also called by BackspaceHandler to delete the closing quotes of an empty string
    // when the opening quotes are removed.
    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int) =
        NixTypes.STRING_OPEN === iterator.tokenType

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean {
        val openingToken = iterator.tokenType
        if (iterator.end != offset + 1) {
            return false // The caret isn't behind the opening quote.
        }
        if (openingToken === NixTypes.STRING_OPEN) {
            // Insert closing quotes only if we would otherwise get a non-closed string at the end of the line.
            val doc = editor.document
            val lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset))
            while (true) {
                val lastToken = iterator.tokenType
                iterator.advance()
                if (iterator.atEnd() || iterator.start >= lineEnd) {
                    return lastToken in NixTokenSets.STRING_ANY && lastToken !in NixTokenSets.CLOSING_QUOTES
                }
            }
        } else if (openingToken === NixTypes.IND_STRING_OPEN) {
            // Insert closing quotes only if we would otherwise get a non-closed string at the end of the file.
            while (true) {
                val lastToken = iterator.tokenType
                iterator.advance()
                if (iterator.atEnd()) {
                    return lastToken in NixTokenSets.STRING_ANY && lastToken !in NixTokenSets.CLOSING_QUOTES
                }
            }
        }
        return false
    }

    // Not sure why we need this. It seems to enable some special handling for escape sequences in IDEA.
    override fun isInsideLiteral(iterator: HighlighterIterator) =
        iterator.tokenType in NixTokenSets.STRING_ANY

    override fun getClosingQuote(iterator: HighlighterIterator, offset: Int): CharSequence? {
        // May need to retreat iterator by one token.
        // In contrast to all the other methods, this method is called with the offset behind the inserted quote.
        // However, the iterator may already be at the right location if the offset is at the end of the file.
        if (iterator.end != offset) {
            iterator.retreat()
            if (iterator.atEnd()) {
                return null // There was no previous token
            }
        }
        return when (iterator.tokenType) {
            NixTypes.STRING_OPEN -> "\""
            NixTypes.IND_STRING_OPEN -> "''"
            else -> null
        }
    }
}
