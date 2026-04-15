package org.nixos.idea.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.nixos.idea.file.NixFile
import org.nixos.idea.psi.NixTokenSets
import org.nixos.idea.psi.NixTokenType
import org.nixos.idea.psi.NixTypes
import org.nixos.idea.psi.NixTypes.*

internal class NixParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = NixLexer()

    override fun getWhitespaceTokens(): TokenSet = NixTokenSets.WHITE_SPACES

    override fun getCommentTokens(): TokenSet = NixTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = NixTokenSets.STRING_LITERALS

    override fun createParser(project: Project?): PsiParser = NixParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = NixFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): SpaceRequirements {
        val leftType: NixTokenType? = left.elementType as? NixTokenType
        val rightType: NixTokenType? = right.elementType as? NixTokenType

        return when (leftType) {
            SCOMMENT -> SpaceRequirements.MUST_LINE_BREAK

            DOLLAR if rightType === LCURLY -> SpaceRequirements.MUST_NOT

            // path segment, antiquotation or PATH_END on the right
            PATH_SEGMENT -> SpaceRequirements.MUST_NOT

            in NixTokenSets.MIGHT_COLLAPSE_WITH_ID if rightType in NixTokenSets.MIGHT_COLLAPSE_WITH_ID
                -> SpaceRequirements.MUST

            // path segment or antiquotation on the left
            leftType if rightType === PATH_END -> SpaceRequirements.MUST_NOT

            else -> SpaceRequirements.MAY
        }
    }

    override fun createElement(node: ASTNode): PsiElement = Factory.createElement(node)

}

val FILE: IFileElementType = IFileElementType(NixLanguage)
