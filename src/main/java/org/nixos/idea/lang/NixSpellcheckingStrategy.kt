package org.nixos.idea.lang

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.IdentifierSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.spellchecker.tokenizer.TokenizerBase
import org.nixos.idea.psi.NixIdentifier
import org.nixos.idea.psi.NixPsiUtil
import org.nixos.idea.psi.NixStringText

/**
 * Enables spell checking for Nix files.
 *
 * @see [Spell Checking Documentation](https://plugins.jetbrains.com/docs/intellij/spell-checking.html)
 *
 * @see [Spell Checking Tutorial](https://plugins.jetbrains.com/docs/intellij/spell-checking-strategy.html)
 */
internal class NixSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun getTokenizer(element: PsiElement?): Tokenizer<*> = when (element) {

        is NixIdentifier if NixPsiUtil.isDeclaration(element) -> IDENTIFIER_TOKENIZER

        is NixStringText -> TEXT_TOKENIZER

        else -> super.getTokenizer(element)
    }
}

// TODO: Implement SuppressibleSpellcheckingStrategy
//  https://plugins.jetbrains.com/docs/intellij/spell-checking.html#suppressing-spellchecking
// TODO: Suggest rename-refactoring for identifiers (when rename refactoring is supported)
private val IDENTIFIER_TOKENIZER: Tokenizer<NixIdentifier> = TokenizerBase.create(IdentifierSplitter.getInstance())
