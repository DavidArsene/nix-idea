package org.nixos.idea.settings.ui

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.RainbowColorSettingsPage
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.nixos.idea.icon.NixIcons
import org.nixos.idea.lang.NixLanguage
import org.nixos.idea.lang.highlighter.NixSyntaxHighlighter
import org.nixos.idea.lang.highlighter.NixTextAttributes
import org.nixos.idea.lang.highlighter.RAINBOW_ATTRIBUTES
import javax.swing.Icon

internal class NixColorSettingsPage : RainbowColorSettingsPage {
    override fun getLanguage() = NixLanguage

    // I think this method shall return true if the NixRainbowVisitor will always override the color of the given
    // key. Unfortunately this method is not documented, but I assume the semantic highlighting will avoid using
    // colors which correspond to attribute keys for which this method returns false.
    override fun isRainbowType(type: TextAttributesKey?) = type in RAINBOW_ATTRIBUTES

    override fun getIcon(): Icon = NixIcons.FILE

    override fun getHighlighter() = NixSyntaxHighlighter

    @NonNls
    override fun getDemoText() = $$"""
        /* This code demonstrates the syntax highlighting for the Nix Expression Language */
        let
            <variable>literals</variable>.null = <literal>null</literal>;
            <variable>literals</variable>.boolean = <literal>true</literal>;
            <variable>literals</variable>.number = 42;
            <variable>literals</variable>.string1 = "This is a normal string";
            <variable>literals</variable>.string2 = ''
                Broken escape sequence:  \${<variable>literals</variable>.number}
                Escaped interpolation:   ''${<variable>literals</variable>.number}
                Generic escape sequence: $''\{<variable>literals</variable>.number}
                '';
            <variable>literals</variable>.paths = [/etc/gitconfig ~/.gitconfig .git/config];
            # Note that unquoted URIs were deperecated by RFC 45
            <variable>literals</variable>.uri = https://github.com/NixOS/rfcs/pull/45;
        in {
            inherit (<variable>literals</variable>) number string1 string2 paths uri;
            nixpkgs = <import>import</import> <nixpkgs>;
            baseNames = <builtin>map</builtin> <builtin>baseNameOf</builtin> <variable>literals</variable>.paths;
            f = { <parameter>multiply</parameter> ? 1, <parameter>add</parameter> ? 0, ... }@<parameter>args</parameter>:
                <builtin>builtins</builtin>.<builtin>mapAttrs</builtin> (<parameter>name</parameter>: <parameter>value</parameter>: <parameter>multiply</parameter> * <parameter>value</parameter> + <parameter>add</parameter>) <parameter>args</parameter>;
        }
        """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap() = ADDITIONAL_HIGHLIGHTING_TAG

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "Nix"
}

private val DESCRIPTORS = arrayOf(
    descriptor("Keyword", NixTextAttributes.KEYWORD),
    descriptor("Operators//Semicolon", NixTextAttributes.SEMICOLON),
    descriptor("Operators//Comma", NixTextAttributes.COMMA),
    descriptor("Operators//Dot", NixTextAttributes.DOT),
    descriptor("Operators//Assignment operator", NixTextAttributes.ASSIGN),
    descriptor("Operators//Colon", NixTextAttributes.COLON),
    descriptor("Operators//At sign (@)", NixTextAttributes.AT),
    descriptor("Operators//Ellipsis", NixTextAttributes.ELLIPSIS),
    descriptor("Operators//Other operators", NixTextAttributes.OPERATION_SIGN),
    descriptor("Braces//Parentheses", NixTextAttributes.PARENTHESES),
    descriptor("Braces//Curly braces", NixTextAttributes.BRACES),
    descriptor("Braces//Brackets", NixTextAttributes.BRACKETS),
    descriptor("Variables and Attributes//Other identifier", NixTextAttributes.IDENTIFIER),
    descriptor("Variables and Attributes//Local variable", NixTextAttributes.LOCAL_VARIABLE),
    descriptor("Variables and Attributes//Function parameter", NixTextAttributes.PARAMETER),
    descriptor("Built-in constants and functions//Literals", NixTextAttributes.LITERAL),
    descriptor("Built-in constants and functions//Import function", NixTextAttributes.IMPORT),
    descriptor("Built-in constants and functions//Other built-ins", NixTextAttributes.BUILTIN),
    descriptor("Literals and Values//Number", NixTextAttributes.NUMBER),
    descriptor("Literals and Values//String", NixTextAttributes.STRING),
    descriptor("Literals and Values//Escape sequence", NixTextAttributes.STRING_ESCAPE),
    descriptor("Literals and Values//Path", NixTextAttributes.PATH),
    descriptor("Literals and Values//URI", NixTextAttributes.URI),
    descriptor("Comments//Line comment", NixTextAttributes.LINE_COMMENT),
    descriptor("Comments//Block comment", NixTextAttributes.BLOCK_COMMENT),
)

private val ADDITIONAL_HIGHLIGHTING_TAG = mapOf(
    "builtin" to NixTextAttributes.BUILTIN,
    "import" to NixTextAttributes.IMPORT,
    "literal" to NixTextAttributes.LITERAL,
    "variable" to NixTextAttributes.LOCAL_VARIABLE,
    "parameter" to NixTextAttributes.PARAMETER
)

private fun descriptor(
    @Nls(capitalization = Nls.Capitalization.Sentence) displayName: @Nls(capitalization = Nls.Capitalization.Sentence) String,
    key: TextAttributesKey
) = AttributesDescriptor(displayName, key)
