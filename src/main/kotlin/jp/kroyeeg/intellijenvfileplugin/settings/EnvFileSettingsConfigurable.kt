package jp.kroyeeg.intellijenvfileplugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import jp.kroyeeg.intellijenvfileplugin.services.EnvFileService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class EnvFileSettingsConfigurable(private val project: Project) : Configurable {
    private var panel: JPanel? = null
    private var pathField: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "load-env-file"

    override fun createComponent(): JComponent {
        val root = JPanel(BorderLayout(8, 8))
        val label = JBLabel(".env path (file or directory):")
        val field = TextFieldWithBrowseButton()
        field.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
            val selected = com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, project, null)
            selected?.let {
                val ioFile = com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile(it)
                field.text = ioFile.path
            }
        }
        field.preferredSize = Dimension(400, field.preferredSize.height)

        val inputPanel = JPanel(BorderLayout(8, 8))
        inputPanel.add(label, BorderLayout.WEST)
        inputPanel.add(field, BorderLayout.CENTER)

        root.add(inputPanel, BorderLayout.NORTH)

        pathField = field
        panel = root
        return root
    }

    override fun isModified(): Boolean {
        val current = EnvFileService.getInstance(project).state.envPath ?: ""
        val ui = pathField?.textField?.text ?: ""
        return current != ui
    }

    override fun apply() {
        val service = EnvFileService.getInstance(project)
        val text = pathField?.textField?.text?.trim()
        service.state.envPath = if (text.isNullOrEmpty()) null else text
        // Reload variables so that new setting takes effect on next Run Config update
        service.update()
    }

    override fun reset() {
        val current = EnvFileService.getInstance(project).state.envPath
        pathField?.textField?.text = current ?: ""
    }

    override fun disposeUIResources() {
        panel = null
        pathField = null
    }
}
