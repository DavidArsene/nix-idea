package org.nixos.idea.psi

import com.intellij.openapi.diagnostic.Logger
import org.nixos.idea.psi.impl.NixStdAttrImpl
import org.nixos.idea.psi.impl.NixStringAttrImpl
import org.nixos.idea.util.NixStringUtil.parse
import java.util.AbstractCollection
import java.util.stream.Stream

object NixPsiUtil {
    private val LOG = Logger.getInstance(NixPsiUtil::class.java)

    fun isRecursive(attrs: NixExprAttrs) =
        attrs.node.findChildByType(NixTypes.REC) != null ||
                attrs.node.findChildByType(NixTypes.LET) != null

    fun isLegacyLet(attrs: NixExprAttrs) = attrs.node.findChildByType(NixTypes.LET) != null

    fun getParameters(lambda: NixExprLambda): Collection<NixParameter> {
        val mainParam = lambda.getArgument()
        val formalsHolder = lambda.getFormals()
        val formals = formalsHolder?.getFormalList() ?: listOf<NixFormal>()

        return object : AbstractCollection<NixParameter>() {
            override fun iterator() = stream().iterator()

            override fun stream(): Stream<NixParameter> {
                return Stream.concat(
                    Stream.ofNullable<NixArgument?>(lambda.getArgument()),
                    formals.stream()
                )
            }

            override val size: Int = (if (mainParam == null) 0 else 1) + formals.size
        }
    }

    fun getArguments(app: NixExprApp): List<NixExpr> {
        val expressions = app.getExprList()
        return expressions.subList(1, expressions.size)
    }

	/**
	 * Returns the static name of an attribute.
	 * Is `null` for dynamic attributes.
	 *
	 * @param attr the attribute
	 * @return the name of the attribute or `null`
	 */
	@JvmStatic
	fun getAttributeName(attr: NixAttr): String? = when (attr) {
		is NixStdAttrImpl -> attr.text

		is NixStringAttrImpl -> {
			val string = attr.stdString
			val stringParts = string?.getStringParts() ?: return null
			if (stringParts.size != 1)
				return null

			parse(stringParts[0] as? NixStringText ?: return null)
		}

		else -> {
			LOG.error("Unexpected NixAttr implementation: " + attr.javaClass)
			null
		}

	}

	@JvmStatic
	fun isDeclaration(identifier: NixIdentifier) = when (identifier) {
		is NixParameterName -> true
		is NixAttr -> isDeclaration(identifier as NixAttr)
		else -> false
	}

    fun isDeclaration(attr: NixAttr): Boolean {
        val attrPath = attr.parent as? NixAttrPath ?: return false
        return attrPath.parent is NixBindAttr
    }
}
