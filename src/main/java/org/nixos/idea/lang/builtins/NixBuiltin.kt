package org.nixos.idea.lang.builtins

import org.nixos.idea.util.NixVersion

class NixBuiltin private constructor(
    //region Instance members
    val name: String,
    val since: NixVersion?,
    val featureFlag: String?,
     val featureFlagIntegration: NixVersion?,
    val highlightingType: HighlightingType
) {
    private val global: Boolean = GLOBAL_SCOPE.contains(name)

    //endregion
    //region Inner classes
    enum class HighlightingType {
        /**
         * Builtins which are casually described as literals.
         * Specifically, only “`true`”, “`null`”, and “`false` have this type”.
         */
        LITERAL,

        /**
         * Import function.
         */
        IMPORT,

        /**
         * Any other builtin which does not match any of the other types.
         */
        OTHER,
    } //endregion

    companion object {
        //region Constants
        /**
         * List of builtins which are available in the global scope (i.e. without “builtins.” prefix).
         * To verify this list against your installation, start `nix repl` and press <kbd>Tab</kbd>.
         */
        private val GLOBAL_SCOPE = setOf(
            "false",
            "null",
            "true",
            "abort",
            "baseNameOf",
            "break",
            "builtins",
            "derivation",
            "derivationStrict",
            "dirOf",
            "fetchGit",
            "fetchMercurial",
            "fetchTarball",
            "fetchTree",
            "fromTOML",
            "import",
            "isNull",
            "map",
            "placeholder",
            "removeAttrs",
            "scopedImport",
            "throw",
            "toString"
        )

        /**
         * List of all builtins. To verify this list against your installation, start `nix repl`,
         * type `builtins.`, and press <kbd>Tab</kbd>.
         */
        private val BUILTINS = mapOf(
            builtin("false", highlightingType = HighlightingType.LITERAL),
            builtin("null", highlightingType = HighlightingType.LITERAL),
            builtin("true", highlightingType = HighlightingType.LITERAL),
            builtin("import", highlightingType = HighlightingType.IMPORT),
            builtin("abort"),
            builtin("add"),
            builtin("addErrorContext"),
            builtin("all"),
            builtin("any"),
            builtin("appendContext"),
            builtin("attrNames"),
            builtin("attrValues"),
            builtin("baseNameOf"),
            builtin("bitAnd"),
            builtin("bitOr"),
            builtin("bitXor"),
            builtin("break", NixVersion.V2_09),
            builtin("builtins"),
            builtin("catAttrs"),
            builtin("ceil", NixVersion.V2_04),
            builtin("compareVersions"),
            builtin("concatLists"),
            builtin("concatMap"),
            builtin("concatStringsSep"),
            builtin("currentSystem"),
            builtin("currentTime"),
            builtin("deepSeq"),
            builtin("derivation"),
            builtin("derivationStrict"),
            builtin("dirOf"),
            builtin("div"),
            builtin("elem"),
            builtin("elemAt"),
            builtin("fetchClosure", NixVersion.V2_08, "fetch-closure"),
            builtin("fetchGit"),
            builtin("fetchMercurial"),
            builtin("fetchTarball"),
            builtin("fetchTree", NixVersion.V2_04),
            builtin("fetchurl"),
            builtin("filter"),
            builtin("filterSource"),
            builtin("findFile"),
            builtin("floor", NixVersion.V2_04),
            builtin("foldl'"),
            builtin("fromJSON"),
            builtin("fromTOML"),
            builtin("functionArgs"),
            builtin("genList"),
            builtin("genericClosure"),
            builtin("getAttr"),
            builtin("getContext"),
            builtin("getEnv"),
            builtin("getFlake", NixVersion.V2_04, "flakes"),
            builtin("groupBy", NixVersion.V2_05),
            builtin("hasAttr"),
            builtin("hasContext"),
            builtin("hashFile"),
            builtin("hashString"),
            builtin("head"),
            builtin("intersectAttrs"),
            builtin("isAttrs"),
            builtin("isBool"),
            builtin("isFloat"),
            builtin("isFunction"),
            builtin("isInt"),
            builtin("isList"),
            builtin("isNull"),
            builtin("isPath"),
            builtin("isString"),
            builtin("langVersion"),
            builtin("length"),
            builtin("lessThan"),
            builtin("listToAttrs"),
            builtin("map"),
            builtin("mapAttrs"),
            builtin("match"),
            builtin("mul"),
            builtin("nixPath"),
            builtin("nixVersion"),
            builtin("parseDrvName"),
            builtin("partition"),
            builtin("path"),
            builtin("pathExists"),
            builtin("placeholder"),
            builtin("readDir"),
            builtin("readFile"),
            builtin("removeAttrs"),
            builtin("replaceStrings"),
            builtin("scopedImport"),
            builtin("seq"),
            builtin("sort"),
            builtin("split"),
            builtin("splitVersion"),
            builtin("storeDir"),
            builtin("storePath"),
            builtin("stringLength"),
            builtin("sub"),
            builtin("substring"),
            builtin("tail"),
            builtin("throw"),
            builtin("toFile"),
            builtin("toJSON"),
            builtin("toPath"),
            builtin("toString"),
            builtin("toXML"),
            builtin("trace"),
            builtin("traceVerbose", NixVersion.V2_10),
            builtin("tryEval"),
            builtin("typeOf"),
            builtin("unsafeDiscardOutputDependency"),
            builtin("unsafeDiscardStringContext"),
            builtin("unsafeGetAttrPos"),
            builtin("zipAttrsWith", NixVersion.V2_06)
        )

        //endregion
        //region Factories

        private fun builtin(
            name: String,
            since: NixVersion? = null,
            featureFlag: String? = null,
            highlightingType: HighlightingType = HighlightingType.OTHER,
        ) =
            name to NixBuiltin(name, since, featureFlag, null, highlightingType)


        //endregion
        //region Static members
        @JvmStatic
        fun resolveBuiltin(name: String): NixBuiltin? = BUILTINS[name]

        @JvmStatic
        fun resolveGlobal(name: String): NixBuiltin? {
            val builtin = resolveBuiltin(name)
            return if (builtin?.global == true) builtin else null
        }
    }
}
