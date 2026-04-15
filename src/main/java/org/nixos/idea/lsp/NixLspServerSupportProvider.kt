package org.nixos.idea.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.nixos.idea.file.NixFileType
import org.nixos.idea.icon.NixIcons

internal class NixLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
        if (file.fileType !== NixFileType) return

//        val settings = instance
//        if (settings.isEnabled) {
            serverStarter.ensureServerStarted(NixLspServerDescriptor(project))
//        }
    }

    override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?) =
        //, NixLspSettingsConfigurable::class.java)
        LspServerWidgetItem(lspServer, currentFile, NixIcons.FILE)
}
