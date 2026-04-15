package org.nixos.idea.lang.highlighter

import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.nixos.idea.lang.NixLexer
import org.nixos.idea.psi.NixTypes

object NixSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = NixLexer()

    override fun getTokenHighlights(tokenType: IElementType?) = pack(TOKEN_MAP[tokenType])


    private val TOKEN_MAP = mapOf(
        // Keywords
        NixTypes.IF to NixTextAttributes.KEYWORD,
        NixTypes.THEN to NixTextAttributes.KEYWORD,
        NixTypes.ELSE to NixTextAttributes.KEYWORD,
        NixTypes.ASSERT to NixTextAttributes.KEYWORD,
        NixTypes.WITH to NixTextAttributes.KEYWORD,
        NixTypes.LET to NixTextAttributes.KEYWORD,
        NixTypes.IN to NixTextAttributes.KEYWORD,
        NixTypes.REC to NixTextAttributes.KEYWORD,
        NixTypes.INHERIT to NixTextAttributes.KEYWORD,
        NixTypes.OR_KW to NixTextAttributes.KEYWORD,
        // Identifiers
        NixTypes.ID to NixTextAttributes.IDENTIFIER,
        // Operators
        NixTypes.ASSIGN to NixTextAttributes.ASSIGN,
        NixTypes.COLON to NixTextAttributes.COLON,
        NixTypes.SEMI to NixTextAttributes.SEMICOLON,
        NixTypes.COMMA to NixTextAttributes.COMMA,
        NixTypes.DOT to NixTextAttributes.DOT,
        NixTypes.ELLIPSIS to NixTextAttributes.ELLIPSIS,
        NixTypes.AT to NixTextAttributes.AT,
        NixTypes.HAS to NixTextAttributes.OPERATION_SIGN,
        NixTypes.NOT to NixTextAttributes.OPERATION_SIGN,
        NixTypes.TIMES to NixTextAttributes.OPERATION_SIGN,
        NixTypes.DIVIDE to NixTextAttributes.OPERATION_SIGN,
        NixTypes.PLUS to NixTextAttributes.OPERATION_SIGN,
        NixTypes.MINUS to NixTextAttributes.OPERATION_SIGN,
        NixTypes.LT to NixTextAttributes.OPERATION_SIGN,
        NixTypes.GT to NixTextAttributes.OPERATION_SIGN,
        NixTypes.CONCAT to NixTextAttributes.OPERATION_SIGN,
        NixTypes.UPDATE to NixTextAttributes.OPERATION_SIGN,
        NixTypes.LEQ to NixTextAttributes.OPERATION_SIGN,
        NixTypes.GEQ to NixTextAttributes.OPERATION_SIGN,
        NixTypes.EQ to NixTextAttributes.OPERATION_SIGN,
        NixTypes.NEQ to NixTextAttributes.OPERATION_SIGN,
        NixTypes.AND to NixTextAttributes.OPERATION_SIGN,
        NixTypes.OR to NixTextAttributes.OPERATION_SIGN,
        NixTypes.IMPL to NixTextAttributes.OPERATION_SIGN,
        // Parentheses
        NixTypes.LPAREN to NixTextAttributes.PARENTHESES,
        NixTypes.RPAREN to NixTextAttributes.PARENTHESES,
        NixTypes.LBRAC to NixTextAttributes.BRACKETS,
        NixTypes.RBRAC to NixTextAttributes.BRACKETS,
        NixTypes.LCURLY to NixTextAttributes.BRACES,
        NixTypes.RCURLY to NixTextAttributes.BRACES,
        NixTypes.DOLLAR to NixTextAttributes.BRACES,
        // Literals
        NixTypes.INT to NixTextAttributes.NUMBER,
        NixTypes.FLOAT to NixTextAttributes.NUMBER,
        NixTypes.PATH_SEGMENT to NixTextAttributes.PATH,
        NixTypes.SPATH to NixTextAttributes.PATH,
        NixTypes.URI to NixTextAttributes.URI,
        // String literals
        NixTypes.STR to NixTextAttributes.STRING,
        NixTypes.STR_ESCAPE to NixTextAttributes.STRING_ESCAPE,
        NixTypes.STRING_CLOSE to NixTextAttributes.STRING,
        NixTypes.STRING_OPEN to NixTextAttributes.STRING,
        NixTypes.IND_STR to NixTextAttributes.STRING,
        NixTypes.IND_STR_LF to NixTextAttributes.STRING,
        NixTypes.IND_STR_INDENT to NixTextAttributes.STRING,
        NixTypes.IND_STR_ESCAPE to NixTextAttributes.STRING_ESCAPE,
        NixTypes.IND_STRING_CLOSE to NixTextAttributes.STRING,
        NixTypes.IND_STRING_OPEN to NixTextAttributes.STRING,
        // Other
        NixTypes.SCOMMENT to NixTextAttributes.LINE_COMMENT,
        NixTypes.MCOMMENT to NixTextAttributes.BLOCK_COMMENT,
        TokenType.BAD_CHARACTER to HighlighterColors.BAD_CHARACTER
    )
}
