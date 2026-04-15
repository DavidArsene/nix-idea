package org.nixos.idea.lang.references

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.model.search.LeafOccurrence
import com.intellij.model.search.LeafOccurrenceMapper
import com.intellij.model.search.SearchContext
import com.intellij.model.search.SearchService
import com.intellij.psi.PsiElement
import com.intellij.util.Query
import org.nixos.idea.lang.NixLanguage
import org.nixos.idea.lang.references.symbol.NixSymbol
import org.nixos.idea.lang.references.symbol.NixUserSymbol
import org.nixos.idea.psi.NixPsiElement
//import org.nixos.idea.settings.NixSymbolSettings

@Suppress("UnstableApiUsage")
internal class NixUsageSearcher : UsageSearcher, LeafOccurrenceMapper.Parameterized<NixSymbol, Usage> {

    override fun collectImmediateResults(parameters: UsageSearchParameters): List<Usage> =
        if (false /*!NixSymbolSettings.instance.enabled */) mutableListOf()
        else {
            val symbol = parameters.target as? NixUserSymbol ?: return mutableListOf()

            symbol.declarations.map(::NixUsage)
        }


    override fun collectSearchRequest(parameters: UsageSearchParameters): Query<out Usage>? =
        if (false /* !NixSymbolSettings.instance.enabled */) null
        else {
            val symbol = parameters.target as? NixSymbol ?: return null

            SearchService.getInstance()
                .searchWord(parameters.project, symbol.name)
                .inContexts(SearchContext.inCodeHosts(), SearchContext.inCode())
                .inScope(parameters.searchScope)
                .inFilesWithLanguage(NixLanguage)
                .buildQuery(LeafOccurrenceMapper.withPointer(symbol.createPointer(), this))
        }

    override fun mapOccurrence(parameter: NixSymbol, occurrence: LeafOccurrence): Collection<Usage> {
        var element: PsiElement? = occurrence.start
        while (element != null && element !== occurrence.scope) {
            if (element is NixPsiElement) {
                val usages = element.ownReferences
                    .filter { reference -> reference.resolvesTo(parameter) }
                        .map(::NixUsage)

                if (!usages.isEmpty()) {
                    return usages
                }
            }
            element = element.parent
        }
        return listOf()
    }
}
