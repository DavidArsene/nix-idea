package org.nixos.idea.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.util.execution.ParametersListUtil
import org.nixos.idea.file.NixFileType

internal class NixLspServerDescriptor(project: Project) :
    ProjectWideLspServerDescriptor(project, "Nix") {
    private val myCommand: String = "nixd" // FIXME:

    override fun createCommandLine(): GeneralCommandLine {
        val argv = ParametersListUtil.parse(myCommand, false, true)
        return GeneralCommandLine(argv)
    }

    override fun isSupportedFile(file: VirtualFile) = file.fileType === NixFileType
}
