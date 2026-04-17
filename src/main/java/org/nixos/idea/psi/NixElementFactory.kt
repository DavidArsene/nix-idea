package org.nixos.idea.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.children
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import org.nixos.idea.file.NixFile
import org.nixos.idea.file.NixFileType
import java.util.*

object NixElementFactory {
	@JvmStatic
	fun createString(project: Project, code: String): NixString {
		return createElement(project, NixString::class.java, "", code, "")
	}

	@JvmStatic
	fun createStdStringText(project: Project, code: String): NixStringText {
		return createElement(project, NixStringText::class.java, "\"", code, "\"")
	}

	@JvmStatic
	fun createIndStringText(project: Project, code: String): NixStringText {
		return createElement(project, NixStringText::class.java, "''\n", code, "''")
	}

	@JvmStatic
	fun createAttr(project: Project, code: String): NixAttr {
		return createElement(project, NixAttr::class.java, "x.", code, "")
	}

	@JvmStatic
	fun createAttrPath(project: Project, code: String): NixAttrPath {
		return createElement(project, NixAttrPath::class.java, "x.", code, "")
	}

	@JvmStatic
	fun createBind(project: Project, code: String): NixBind {
		return createElement(project, NixBind::class.java, "{", code, "}")
	}

	@JvmStatic
	@Suppress("UNCHECKED_CAST")
	fun <T : NixExpr> createExpr(project: Project, code: String): T {
		return createElement(project, NixExpr::class.java, "", code, "") as T
	}

	@JvmStatic
	fun <T : NixPsiElement?> createElement(
		project: Project, type: Class<T>,
		prefix: String, text: String, suffix: String
	): T = requireNotNull(createElementOrNull(project, type, prefix, text, suffix)) {
		"Invalid ${type.getSimpleName()}: $text"
	}

	private fun <T : NixPsiElement?> createElementOrNull(
		project: Project, type: Class<T>,
		prefix: String, text: String, suffix: String
	): T? {
		val file = createFile(project, prefix + text + suffix)
		var current = file.node.firstChildNode
		var offset = 0
		while (current != null && offset <= prefix.length) {
			val length = current.textLength
			// Check if we have found the right element
			if (offset == prefix.length && length == text.length) {
				val psi = current.psi
				if (type.isInstance(psi)) {
					return if (containsErrors(current)) null else type.cast(psi)
				}
			}
			// Check if we should go into or over this element
			if (offset + length <= prefix.length) {
				offset += length
				current = current.treeNext
			}
			else {
				current = current.firstChildNode
			}
		}
		return null
	}

	private fun containsErrors(node: ASTNode): Boolean {
		var current = node.firstChildNode
		while (current != null) {
			if (current.elementType === TokenType.ERROR_ELEMENT) {
				return true
			}
			var next = current.firstChildNode
			if (next == null) {
				do {
					next = current!!.treeNext
				} while (next == null && (current.treeParent.also { current = it }) !== node)
			}
			current = next
		}
		return false
	}

	// private fun containsErrors(node: ASTNode): Boolean {
	// 	val queue: Queue<ASTNode> = LinkedList()
	// 	queue.add(node)
	// 	while (queue.isNotEmpty()) {
	// 		val current = queue.remove()
	// 		if (current.elementType === TokenType.ERROR_ELEMENT) {
	// 			return true
	// 		}
	// 		current.children().forEach { queue.add(it) }
	// 	}
	// 	return false
	// }

	fun createFile(project: Project, code: String): NixFile =
		PsiFileFactory.getInstance(project).createFileFromText("dummy.nix", NixFileType, code) as NixFile
}
