package org.nixos.idea.util

import com.intellij.lang.ASTNode
import org.jetbrains.annotations.Contract
import org.nixos.idea.psi.NixAntiquotation
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixStdString
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringText
import org.nixos.idea.psi.NixTypes
import kotlin.math.min

/**
 * Utilities for encoding and decoding strings in the Nix Expression Language.
 */
object NixStringUtil {
    /**
     * Returns the source code for a string in the Nix Expression Language.
     * When the returned string is evaluated by a Nix interpreter, the result matches the sting given to this method.
     * The returned string expression is always a double-quoted string.
     *
     * <h4>Example</h4>
     * <pre>`System.out.println(quote("This should be escaped: ${}"));`</pre>
     * The code above prints the following:
     * <pre>"This should be escaped: \${}"</pre>
     *
     * @param unescaped The raw string which shall be the result when the expression is evaluated.
     * @return Source code for a Nix expression which evaluates to the given string.
     */
    @JvmStatic
    @Contract(pure = true)
    fun quote(unescaped: CharSequence): String {
        val builder = StringBuilder()
        builder.append('"')
        escapeStd(builder, unescaped)
        builder.append('"')
        return builder.toString()
    }

    /**
     * Escapes the given string for use in a double-quoted string expression in the Nix Expression Language.
     * Note that it is not safe to combine the results of two method calls with arbitrary input.
     * For example, the following code would generate a broken result.
     * <pre>`StringBuilder b1 = new StringBuilder(), b2 = new StringBuilder();     NixStringUtil.escapeStd(b1, "$");     NixStringUtil.escapeStd(b2, "{''}");     System.out.println(b1.toString() + b2.toString()); `</pre>
     * The result would be the following broken Nix code.
     * <pre>
     * ${''}
    </pre> *
     *
     * @param builder   The target string builder. The result will be appended to the given string builder.
     * @param unescaped The raw string which shall be escaped.
     */
    @JvmStatic
    fun escapeStd(builder: StringBuilder, unescaped: CharSequence) {
        var potentialInterpolation = false

        unescaped.forEach { nextChar ->
            when (nextChar) {
                '"', '\\' -> builder.append('\\').append(nextChar)
                '{' -> if (potentialInterpolation) {
                    builder.setCharAt(builder.length - 1, '\\')
                    builder.append('$').append('{')
                } else {
                    builder.append('{')
                }

                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> builder.append(nextChar)
            }
            potentialInterpolation = nextChar == '$' && !potentialInterpolation
        }
    }

    /**
     * Escapes the given string for use in an indented string expression in the Nix Expression Language.
     * Note that it is not safe to concat the result of two calls of this method.
     *
     * @param builder     The target string builder. The result will be appended to the given string builder.
     * @param unescaped   The raw string which shall be escaped.
     * @param indent      The number as spaces used for indentation
     * @param indentStart Whether the start of the string needs to be indented
     * @param indentEnd   The number as spaces used for indentation in the last line
     */
    @JvmStatic
    fun escapeInd(builder: StringBuilder, unescaped: CharSequence, indent: Int, indentStart: Boolean, indentEnd: Int) {
        var indentStart = indentStart
        val indentStr = " ".repeat(indent)
        var potentialInterpolation = false
        var potentialClosing = false
        for (charIndex in unescaped.indices) {
            val nextChar = unescaped[charIndex]
            if (indentStart && nextChar != '\n') {
                builder.append(indentStr)
                indentStart = false
            }
            when (nextChar) {
                '\'' -> {
                    // Convert `''` to `'''`
                    if (potentialClosing) {
                        builder.append('\'')
                    }
                    builder.append('\'')
                }

                '{' -> // Convert `${` to `''${`, but leave `$${` untouched
                    if (potentialInterpolation) {
                        builder.setLength(builder.length - 1)
                        builder.append("''\${")
                    } else {
                        builder.append('{')
                    }

                '\r' -> builder.append("''\\r")
                '\t' -> builder.append("''\\t")
                '\n' -> {
                    indentStart = true
                    builder.append(nextChar)
                }

                else -> builder.append(nextChar)
            }
            potentialInterpolation = nextChar == '$' && !potentialInterpolation
            potentialClosing = nextChar == '\'' && !potentialClosing
        }
        if (indentStart) {
            builder.append(" ".repeat(min(indent, indentEnd)))
        }
    }

    /**
     * Detects the maximal amount of characters removed from the start of the lines.
     * May return [Integer.MAX_VALUE] if the content of the string is blank.
     *
     * @param string the string from which to get the indentation
     * @return the detected indentation, or [Integer.MAX_VALUE]
     */
    @JvmStatic
    fun detectMaxIndent(string: NixString): Int {
        if (string is NixStdString) return 0
        require(string is NixIndString) { "Unexpected subclass of NixString: " + string.javaClass }

        var result = Int.MAX_VALUE
        var preliminary = 0

        string.stringParts.forEach { part ->
            if (part is NixStringText) {

                var token = part.node.firstChildNode
                while (token != null) {
                    when (token.elementType) {
                        NixTypes.IND_STR_INDENT -> preliminary = min(result, token.textLength)
                        NixTypes.IND_STR_LF -> preliminary = 0
                        NixTypes.IND_STR, NixTypes.IND_STR_ESCAPE -> result = preliminary
                        else -> throw IllegalStateException(token.elementType.toString())
                    }
                    token = token.treeNext
                }
            } else {
                require(part is NixAntiquotation) { "Unexpected part of indented string: " + part.javaClass }
                result = preliminary
            }
        }
        return result
    }

    /**
     * Returns the content of the given part of a string in the Nix Expression Language.
     * All escape sequences are resolved.
     *
     * @param textNode A part of a string.
     * @return The resulting string after resolving all escape sequences.
     */
    @JvmStatic
    fun parse(textNode: NixStringText): String {
        val maxIndent = detectMaxIndent((textNode.parent as NixString?)!!)
        val builder = StringBuilder()
        visit(object : StringVisitor {
            override fun text(text: CharSequence, offset: Int): Boolean {
                builder.append(text)
                return true
            }

            override fun escapeSequence(text: String, offset: Int, escapeSequence: CharSequence): Boolean {
                builder.append(text)
                return true
            }
        }, textNode, maxIndent)
        return builder.toString()
    }

    fun visit(visitor: StringVisitor, textNode: NixStringText, maxIndent: Int) {
        var offset = 0
        var child = textNode.node.firstChildNode
        while (child != null) {
            if (!parse(visitor, child, offset, maxIndent)) {
                break
            }
            offset += child.textLength
            child = child.treeNext
        }
    }

    private fun parse(visitor: StringVisitor, token: ASTNode, offset: Int, maxIndent: Int): Boolean {
        val text = token.chars
        val type = token.elementType
        if (type === NixTypes.STR || type === NixTypes.IND_STR || type === NixTypes.IND_STR_LF) {
            return visitor.text(text, offset)
        } else if (type === NixTypes.IND_STR_INDENT) {
            val end = text.length
            if (end > maxIndent) {
                val remain = text.subSequence(maxIndent, end)
                return visitor.text(remain, offset + maxIndent)
            }
            return true
        } else if (type === NixTypes.STR_ESCAPE) {
            assert(text.length == 2 && text[0] == '\\') { text }
            val c = text[1]
            return visitor.escapeSequence(unescape(c), offset, text)
        } else if (type === NixTypes.IND_STR_ESCAPE) {
            return when (text[2]) {
                '$' -> {
                    assert("''$".contentEquals(text)) { text }
                    visitor.escapeSequence("$", offset, text)
                }

                '\'' -> {
                    assert("'''".contentEquals(text)) { text }
                    visitor.escapeSequence("''", offset, text)
                }

                '\\' -> {
                    assert(text.length == 4 && "''\\".contentEquals(text.subSequence(0, 3))) { text }
                    val c = text[3]
                    visitor.escapeSequence(unescape(c), offset, text)
                }

                else -> throw IllegalStateException("Unknown escape sequence: $text")
            }
        } else {
            throw IllegalStateException("Unexpected token in string: $token")
        }
    }

    private fun unescape(c: Char) = when (c) {
        'n' -> "\n"
        'r' -> "\r"
        't' -> "\t"
        else -> c.toString()
    }

    interface StringVisitor {
        fun text(text: CharSequence, offset: Int): Boolean

        fun escapeSequence(text: String, offset: Int, escapeSequence: CharSequence): Boolean
    }
}
