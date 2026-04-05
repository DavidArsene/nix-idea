package org.nixos.idea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.nixos.idea.settings.SimplePersistentStateComponentHelper.delegate
import java.util.ArrayDeque
import java.util.Collections
import java.util.Deque

@State(
    name = "NixExternalFormatterSettings",
    storages = [Storage(value = NixStoragePaths.TOOLS, roamingType = RoamingType.LOCAL)]
)
class NixExternalFormatterSettings : SimplePersistentStateComponent<NixExternalFormatterSettings.NixState>(NixState()) {

    class NixState : BaseState() {
        var enabled by property(false)
        var command by string()
        var history: Deque<String> by property(ArrayDeque(), { it.isEmpty() })
    }

    var isFormatEnabled: Boolean by delegate(NixState::enabled)
    var formatCommand: String by delegate(NixState::command, NixState::history)
    val commandHistory: Collection<String>
        get() = Collections.unmodifiableCollection(state.history)

    companion object {
        @JvmStatic
        fun getInstance() = ApplicationManager.getApplication().getService(NixExternalFormatterSettings::class.java)!!
    }
}
