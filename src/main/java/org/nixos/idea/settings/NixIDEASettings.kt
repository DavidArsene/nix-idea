package org.nixos.idea.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import org.jetbrains.annotations.Nls
import org.nixos.idea.util.NixPathVerifier
import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class NixIDEASettings internal constructor(project: Project) : SearchableConfigurable {
    private val NIX_IDEA_ID = "NixIDEA Settings"
    private val projectProperties = PropertiesComponent.getInstance(project)
    private val nixitchSettings: JPanel? = null
    private var nixPath: JTextField? = null
    private var nixProfiles: JTextField? = null
    private var nixOtherStores: JTextField? = null
    private var nixRemote: JTextField? = null
    private var nixPkgsConfig: TextFieldWithBrowseButton? = null
    private var nixConfDir: TextFieldWithBrowseButton? = null
    private var nixUserProfileDir: TextFieldWithBrowseButton? = null

    private val settings = listOf<Setting>(
        ResettableEnvField("NIX_PATH", TextComponent(nixPath)),
        ResettableEnvField("NIX_PROFILES", TextComponent(nixProfiles)),
        ResettableEnvField("NIX_OTHER_STORES", TextComponent(nixOtherStores)),
        ResettableEnvField("NIX_REMOTE", TextComponent(nixRemote)),
        ResettableEnvField("NIXPKGS_CONFIG", TextComponent(nixPkgsConfig)),
        ResettableEnvField("NIX_CONF_DIR", TextComponent(nixConfDir)),
        ResettableEnvField("NIX_USER_PROFILE_DIR", TextComponent(nixUserProfileDir))
    )

    init {
        val originalBackground = nixPath!!.getBackground()
        nixPath!!.inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent?): Boolean {
                val tf = input as JTextField
                val npv = NixPathVerifier(tf.text)

                return npv.verify().also {
                    tf.setBackground(
                        if (it) originalBackground
                        //Some parts of the paths are inaccessible
                        //TODO: change to individual path strikeout
                        else JBColor.RED
                    )
                }
            }
        }
    }

    internal interface Setting {
        fun dirty(): Boolean

        fun store()

        fun reset()
    }

    internal interface TextFriend {
        var text: String
    }

    internal class TextComponent : TextFriend {
        private val c: Any?

        constructor(tf: JTextField?) {
            c = tf
        }

        constructor(tfwbb: TextFieldWithBrowseButton?) {
            c = tfwbb
        }

        override var text: String
            get() = when (c) {
                is JTextField -> c.text
                is TextFieldWithBrowseButton -> c.text
                else -> ""
            }
            set(value) {
                when (c) {
                    is JTextField -> c.text = value
                    is TextFieldWithBrowseButton -> c.text = value
                }
            }

    }

    internal inner class ResettableEnvField(var env: String, val tf: TextFriend) : Setting, ChangeListener {
        var id: String = "NIXITCH_$env"
        var previous: String? = null
        var value: String? = null

        init {
            this.tf.text = readStoredEnv(env)
            store()
        }

        override fun dirty() = tf.text != previous

        override fun store() {
            previous = tf.text // previous is now current..
            projectProperties.setValue(id, previous)
        }

        override fun reset() {
            tf.text = previous!!
        }

        override fun stateChanged(e: ChangeEvent?) {
            if (dirty()) store()
        }

        private fun readEnv(env: String) = try {
            System.getenv(env)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

        private fun readStoredEnv(env: String) = projectProperties.getValue("NIXITCH_$env", readEnv(env))
    }

    override fun getId() = NIX_IDEA_ID

    override fun enableSearch(s: String?): Runnable? = null

    @Nls
    override fun getDisplayName(): @Nls String = NIX_IDEA_ID

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent? = nixitchSettings

    override fun isModified(): Boolean = settings.any { it.dirty() }

    @Throws(ConfigurationException::class)
    override fun apply() {
        // update the variables
        settings.forEach(Setting::store)
    }

    override fun reset() {
        // restore from previously applied variables
        settings.forEach(Setting::reset)
    }

    override fun disposeUIResources() = Unit
}
