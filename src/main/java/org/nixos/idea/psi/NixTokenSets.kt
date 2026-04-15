package org.nixos.idea.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

object NixTokenSets {
    /** All tokens representing whitespaces. */
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

    /** All tokens representing comments. */
    val COMMENTS = TokenSet.create(NixTypes.SCOMMENT, NixTypes.MCOMMENT)

    /** Elements representing string literals. Note that these types are used for non-leaf elements, they don't represent tokens. */
    val STRING_LITERALS = TokenSet.create(NixTypes.STD_STRING, NixTypes.IND_STRING)

    /** All token types which represent a keyword. */
    val KEYWORDS = TokenSet.create(
        NixTypes.IF,
        NixTypes.THEN,
        NixTypes.ELSE,
        NixTypes.ASSERT,
        NixTypes.WITH,
        NixTypes.LET,
        NixTypes.IN,
        NixTypes.REC,
        NixTypes.INHERIT,
        NixTypes.OR_KW
    )

    /** All tokens representing opening quotes. */
    val OPENING_QUOTES = TokenSet.create(NixTypes.STRING_OPEN, NixTypes.IND_STRING_OPEN)

    /** All tokens representing closing quotes. */
    val CLOSING_QUOTES = TokenSet.create(NixTypes.STRING_CLOSE, NixTypes.IND_STRING_CLOSE)

    /** All tokens representing text inside a string. */
    val STRING_CONTENT =
        TokenSet.create(NixTypes.STR, NixTypes.STR_ESCAPE, NixTypes.IND_STR, NixTypes.IND_STR_ESCAPE)

    /** All tokens representing any part of a string, except interpolations. */
    val STRING_ANY = TokenSet.orSet(CLOSING_QUOTES, OPENING_QUOTES, STRING_CONTENT)

    /** Tokens would collapse if they were not separated by whitespace. */
    val MIGHT_COLLAPSE_WITH_ID = TokenSet.orSet(
        KEYWORDS, TokenSet.create(
            NixTypes.ID,
            NixTypes.INT,
            NixTypes.FLOAT,
            NixTypes.SPATH,
            NixTypes.PATH_SEGMENT,
            NixTypes.PATH_END,
            NixTypes.URI
        )
    )
}
