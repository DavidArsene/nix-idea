package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import org.nixos.idea.lang.references.NixSymbolDeclaration
import org.nixos.idea.lang.references.symbol.NixUserSymbol
import org.nixos.idea.psi.NixAttr
import org.nixos.idea.psi.NixAttrPath
import org.nixos.idea.psi.NixBind
import org.nixos.idea.psi.NixBindAttr
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixDeclarationHost
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprLambda
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixIdentifier
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.psi.NixPsiUtil
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.Objects

/**
 * Implementation of all instances of [NixDeclarationHost].
 */
internal abstract class AbstractNixDeclarationHost(node: ASTNode) : AbstractNixPsiElement(node), NixDeclarationHost {
	private var mySymbols: Symbols? = null

	override val isDeclaringVariables: Boolean = when (this) {
		is NixExprLet, is NixExprLambda -> true

		is NixExprAttrs -> NixPsiUtil.isRecursive(this)

		else -> {
			LOG.error("Unknown subclass: $javaClass")
			false
		}
	}

	override fun getSymbolForScope(variableName: String): NixUserSymbol? {
		// Note: When we want to support dynamic attributes in the future, they must be ignored by this method.
		assert(isDeclaringVariables) { "getSymbolForScope(...) must not be called when isDeclaringVariables() returns false" }
		return this.symbols.getSymbolForScope(variableName)
	}

	override fun getSymbol(attributePath: List<String>) = this.symbols.getSymbol(attributePath)

	override fun getDeclarations(attributePath: List<String>) = this.symbols.getDeclarations(attributePath)

	private val symbols: Symbols
		get() {
			// val settings = instance
			val symbols = mySymbols
			if (symbols == null /* || symbols.isOutdated(settings) */) {
				MY_SYMBOLS.compareAndSet(this, symbols, initSymbols(/* settings */))
				Objects.requireNonNull<Symbols?>(
					mySymbols,
					"initSymbols() must not return null"
				)
			}
			return mySymbols!!
		}

	private fun initSymbols(/* settings: NixSymbolSettings */): Symbols {
		val symbols = Symbols(/* settings */)
		// if (!instance.enabled) {
		//    return symbols
		// }
		when (this) {
			is NixExprLet -> collectBindDeclarations(symbols, this.getBindList(), true)

			is NixExprAttrs -> collectBindDeclarations(symbols, this.getBindList(), NixPsiUtil.isLegacyLet(this))

			is NixExprLambda ->
				for (parameter in NixPsiUtil.getParameters(this)) {
					val identifier: NixIdentifier = parameter.getIdentifier()
					symbols.addParameter(parameter, identifier)
				}

			else -> LOG.error("Unknown subclass: $javaClass")
		}
		return symbols
	}

	private fun collectBindDeclarations(result: Symbols, bindList: List<NixBind>, isVariable: Boolean) {
		val type = if (isVariable) NixUserSymbol.Type.VARIABLE else NixUserSymbol.Type.ATTRIBUTE
		for (bind in bindList) {
			when (bind) {
				is NixBindAttr -> result.addBindAttr(bind, bind.getAttrPath(), type)

				is NixBindInherit ->
					for (inheritedAttribute in bind.getAttributes()) {
						result.addInherit(bind, inheritedAttribute, type, bind.getSource() != null)
					}

				else -> LOG.error("Unexpected NixBind implementation: " + bind.javaClass)
			}
		}
	}

	private fun checkDeclarationHost(element: NixPsiElement): Boolean {
		if (element is AbstractNixPsiElement) {
			if (element.declarationHost === this) {
				return true
			}
			LOG.error("Element must belong to this declaration host")
		}
		else {
			LOG.error("Unexpected NixPsiElement implementation: " + element.javaClass)
		}
		return false
	}

	private inner class Symbols(/* settings: NixSymbolSettings */) {
		private val mySymbols: MutableMap<List<String>, NixUserSymbol> =
			HashMap()
		private val myDeclarationsBySymbol: MutableMap<List<String>, MutableList<NixSymbolDeclaration>> =
			HashMap()
		private val myDeclarationsByElement: MutableMap<NixPsiElement, MutableList<NixSymbolDeclaration>> =
			HashMap()
		private val myVariables: MutableSet<String> = HashSet()
		// private val mySettingsModificationCount = settings.getStateModificationCount()

		// fun isOutdated(settings: NixSymbolSettings) =
		// 	mySettingsModificationCount != settings.getStateModificationCount()

		fun addBindAttr(element: NixPsiElement, attrPath: NixAttrPath, type: NixUserSymbol.Type) {
			var type = type
			if (!checkDeclarationHost(element)) {
				return
			}

			val elementName = attrPath.text
			val path: MutableList<String> = mutableListOf()
			for (attr in attrPath.getAttrList()) {
				val name = NixPsiUtil.getAttributeName(attr) ?: return
				path.add(name)
				add(element, attr, path, type, true, elementName, null)
				type = NixUserSymbol.Type.ATTRIBUTE
			}
		}

		fun addInherit(
			element: NixPsiElement, attr: NixAttr,
			type: NixUserSymbol.Type, exposeAsVariable: Boolean
		) {
			val name = NixPsiUtil.getAttributeName(attr)
			if (checkDeclarationHost(element) && name != null) {
				add(
					element, attr, listOf(name),
					type, exposeAsVariable, attr.text, "inherit"
				)
			}
		}

		fun addParameter(element: NixPsiElement, identifier: NixIdentifier) {
			if (checkDeclarationHost(element)) {
				add(
					element, identifier,
					listOf(identifier.text),
					NixUserSymbol.Type.PARAMETER, true,
					identifier.text, "lambda"
				)
			}
		}

		fun add(
			element: NixPsiElement,
			identifier: NixPsiElement,
			attributePath: List<String>,
			type: NixUserSymbol.Type,
			exposeAsVariable: Boolean,
			elementName: String,
			elementType: String?
		) {
			assert(checkDeclarationHost(element))
			val attributePathCopy = ArrayList(attributePath)

			val symbol = mySymbols.computeIfAbsent(
				attributePathCopy
			) { path: List<String> -> NixUserSymbol(this@AbstractNixDeclarationHost, path, type) }
			if (exposeAsVariable && attributePath.size == 1) {
				myVariables.add(attributePath[0])
			}

			val declaration = NixSymbolDeclaration(element, identifier, symbol, elementName, elementType)
			myDeclarationsBySymbol.computeIfAbsent(attributePathCopy) { mutableListOf() }
				.add(declaration)
			myDeclarationsByElement.computeIfAbsent(element) { mutableListOf() }
				.add(declaration)
		}

		fun getSymbolForScope(variableName: String): NixUserSymbol? {
			return if (myVariables.contains(variableName)) mySymbols[listOf(variableName)] else null
		}

		fun getSymbol(attributePath: List<String>) = mySymbols[attributePath]

		fun getDeclarations(attributePath: List<String>): List<NixSymbolDeclaration> {
			return myDeclarationsBySymbol.getOrDefault(attributePath, listOf())
		}

		fun getDeclarations(element: NixPsiElement): List<NixSymbolDeclaration> {
			return myDeclarationsByElement.getOrDefault(element, listOf())
		}
	}

	override fun subtreeChanged() {
		super.subtreeChanged()
		mySymbols = null
	}

	init {
		if ((this !is NixExprLet) && (this !is NixExprAttrs) && (this !is NixExprLambda)) {
			LOG.error("Unknown subclass: $javaClass")
		}
	}

	companion object {
		private val LOG = Logger.getInstance(AbstractNixDeclarationHost::class.java)

		fun getDeclarations(element: AbstractNixPsiElement): Collection<NixSymbolDeclaration> {
			val declarationHost = element.declarationHost ?: return listOf()
			return declarationHost.symbols.getDeclarations(element)
		}

		// VarHandle mechanics
		private val MY_SYMBOLS: VarHandle

		init {
			try {
				val l = MethodHandles.lookup()
				MY_SYMBOLS = l.findVarHandle(AbstractNixDeclarationHost::class.java, "mySymbols", Symbols::class.java)
			}
			catch (e: ReflectiveOperationException) {
				throw ExceptionInInitializerError(e)
			}
		}
	}
}
