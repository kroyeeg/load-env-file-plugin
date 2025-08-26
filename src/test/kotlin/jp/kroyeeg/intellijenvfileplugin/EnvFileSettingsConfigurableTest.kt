package jp.kroyeeg.intellijenvfileplugin

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import jp.kroyeeg.intellijenvfileplugin.settings.EnvFileSettingsConfigurable
import jp.kroyeeg.intellijenvfileplugin.services.EnvFileService
import java.awt.Component
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.testFramework.runInEdtAndWait

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class EnvFileSettingsConfigurableTest : BasePlatformTestCase() {

    private fun runEdt(block: () -> Unit) {
        runInEdtAndWait(block)
    }

    private fun findField(root: JComponent): TextFieldWithBrowseButton {
        fun dfs(c: Component): TextFieldWithBrowseButton? {
            if (c is TextFieldWithBrowseButton) return c
            if (c is JPanel) {
                c.components.forEach { child ->
                    val found = dfs(child)
                    if (found != null) return found
                }
            }
            return null
        }
        return requireNotNull(dfs(root)) { "TextFieldWithBrowseButton not found in component tree" }
    }

    fun testResetPopulatesFieldFromServiceState() {
        val service = EnvFileService.getInstance(project)
        val base = project.basePath!!
        val expectedPath = File(base, "configA").apply { mkdirs() }.absolutePath
        service.state.envPath = expectedPath

        val configurable = EnvFileSettingsConfigurable(project)
        runEdt {
            val root = configurable.createComponent()
            configurable.reset()

            val field = findField(root)
            assertEquals(expectedPath, field.text)
            // isModified should be false when UI matches state
            assertFalse(configurable.isModified)
        }
    }

    fun testIsModifiedReflectsChanges() {
        val service = EnvFileService.getInstance(project)
        service.state.envPath = null

        val configurable = EnvFileSettingsConfigurable(project)
        runEdt {
            val root = configurable.createComponent()
            configurable.reset() // makes field empty string from null state

            val field = findField(root)
            assertEquals("", field.text)
            assertFalse(configurable.isModified)

            field.text = "/tmp/foo"
            assertTrue(configurable.isModified)

            // Sync state with UI and then ensure isModified becomes false
            service.state.envPath = field.text
            assertFalse(configurable.isModified)
        }
    }

    fun testApplyUpdatesServiceAndLoadsFromDirectory() {
        val base = project.basePath!!
        val dir = File(base, "configB").apply { mkdirs() }
        File(dir, ".env").writeText("DIR_VAR=dirValue\n")

        val service = EnvFileService.getInstance(project)
        service.state.envPath = null

        val configurable = EnvFileSettingsConfigurable(project)
        runEdt {
            val root = configurable.createComponent()
            val field = findField(root)
            field.text = dir.absolutePath

            // Before apply, state should still be null
            assertNull(service.state.envPath)

            configurable.apply()

            // After apply, envPath should be updated and variables reloaded
            assertEquals(dir.absolutePath, service.state.envPath)
            val vars = service.state.variables.associate { it.key to it.value }
            assertEquals("dirValue", vars["DIR_VAR"])
        }
    }

    fun testApplyUpdatesServiceAndLoadsFromFile() {
        val base = project.basePath!!
        val dir = File(base, "configC").apply { mkdirs() }
        val envFile = File(dir, "custom.env").apply { writeText("FILE_VAR=fileValue\n") }

        val service = EnvFileService.getInstance(project)
        service.state.envPath = null

        val configurable = EnvFileSettingsConfigurable(project)
        runEdt {
            val root = configurable.createComponent()
            val field = findField(root)
            field.text = envFile.absolutePath
            configurable.apply()

            assertEquals(envFile.absolutePath, service.state.envPath)
            val vars = service.state.variables.associate { it.key to it.value }
            assertEquals("fileValue", vars["FILE_VAR"])        
        }
    }
}
