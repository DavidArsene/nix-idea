package org.nixos.idea.lang.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object NixTextAttributes {
    val KEYWORD = createTextAttributesKey("NIX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

    val SEMICOLON = createTextAttributesKey("NIX_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
    val COMMA = createTextAttributesKey("NIX_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val DOT = createTextAttributesKey("NIX_DOT", DefaultLanguageHighlighterColors.DOT)
    val ASSIGN = createTextAttributesKey("NIX_ASSIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val COLON = createTextAttributesKey("NIX_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val AT = createTextAttributesKey("NIX_AT", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val ELLIPSIS = createTextAttributesKey("NIX_ELLIPSIS", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val OPERATION_SIGN = createTextAttributesKey("NIX_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN)

    val PARENTHESES = createTextAttributesKey("NIX_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val BRACES = createTextAttributesKey("NIX_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val BRACKETS = createTextAttributesKey("NIX_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)

    // TODO: auto-jvmfielder
//    @JvmField
    val IDENTIFIER = createTextAttributesKey("NIX_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    val LITERAL = createTextAttributesKey("NIX_LITERAL", DefaultLanguageHighlighterColors.KEYWORD)
    val IMPORT = createTextAttributesKey("NIX_IMPORT", DefaultLanguageHighlighterColors.KEYWORD)
//    @JvmField
    val BUILTIN = createTextAttributesKey("NIX_BUILTIN", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
//    @JvmField
    val LOCAL_VARIABLE = createTextAttributesKey("NIX_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
//    @JvmField
    val PARAMETER = createTextAttributesKey("NIX_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)

    val STRING = createTextAttributesKey("NIX_STRING", DefaultLanguageHighlighterColors.STRING)
    val STRING_ESCAPE = createTextAttributesKey("NIX_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val URI = createTextAttributesKey("NIX_URI", DefaultLanguageHighlighterColors.STRING)
    val PATH = createTextAttributesKey("NIX_PATH", DefaultLanguageHighlighterColors.STRING)
    val NUMBER = createTextAttributesKey("NIX_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

    val LINE_COMMENT = createTextAttributesKey("NIX_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BLOCK_COMMENT = createTextAttributesKey("NIX_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
}
