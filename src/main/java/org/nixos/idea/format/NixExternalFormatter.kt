package org.nixos.idea.format

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile
import com.intellij.util.execution.ParametersListUtil
import org.nixos.idea.file.NixFile
import org.nixos.idea.settings.NixExternalFormatterSettings.Companion.getInstance
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.EnumSet

class NixExternalFormatter : AsyncDocumentFormattingService() {
    override fun getNotificationGroupId(): String = name

    @NlsSafe
    override fun getName(): @NlsSafe String = "NixIDEA"

    override fun getFeatures() = EnumSet.noneOf(FormattingService.Feature::class.java)!!

    override fun canFormat(psiFile: PsiFile): Boolean = psiFile is NixFile

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val nixSettings = getInstance()
        if (!nixSettings.isFormatEnabled) {
            return null
        }

        val ioFile = request.ioFile ?: return null

        val command = nixSettings.formatCommand
        val argv = ParametersListUtil.parse(command, false, true)
        val commandLine = GeneralCommandLine(argv)

        return object : FormattingTask {
            private var handler: OSProcessHandler? = null
            private var canceled = false

            override fun run() {
                synchronized(this) {
                    if (canceled) {
                        return
                    }
                    try {
                        handler = OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8))
                    } catch (e: ExecutionException) {
                        request.onError("NixIDEA", e.message!!)
                        return
                    }
                }
                handler!!.addProcessListener(object : CapturingProcessAdapter() {
                    override fun processTerminated(event: ProcessEvent) {
                        val exitCode = event.exitCode
                        if (exitCode == 0) {
                            request.onTextReady(output.stdout)
                        } else {
                            request.onError("NixIDEA", output.stderr)
                        }
                    }
                })
                handler!!.startNotify()
                try {
                    val processInput = handler!!.processInput
                    Files.copy(ioFile.toPath(), processInput)
                    processInput.close()
                } catch (e: IOException) {
                    handler!!.destroyProcess()
                    request.onError("NixIDEA", e.message!!)
                }
            }

            @Synchronized
            override fun cancel(): Boolean {
                canceled = true
                if (handler != null) {
                    handler!!.destroyProcess()
                }
                return true
            }

            override fun isRunUnderProgress(): Boolean = true
        }
    }
}
