package org.nixos.idea.lang

import com.intellij.codeInsight.generation.IndentedCommenter

internal class NixCommenter : IndentedCommenter {
    override fun getLineCommentPrefix(): String = "#"

    override fun getBlockCommentPrefix(): String = "/*"

    override fun getBlockCommentSuffix(): String = "*/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null

    override fun forceIndentedLineComment(): Boolean = true
}
