package jp.kroyeeg.intellijenvfileplugin

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import jp.kroyeeg.intellijenvfileplugin.services.EnvFileService
import jp.kroyeeg.intellijenvfileplugin.run.EnvRunConfigurationListener
import java.io.File

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class LoadEnvFilePluginTest : BasePlatformTestCase() {

    fun testLoadsFromProjectRootEnv() {
        val base = project.basePath!!
        val envFile = File(base, ".env")
        envFile.writeText("VAR1=foo\n")

        val service = EnvFileService.getInstance(project)
        service.state.envPath = null
        service.update()

        val vars = service.state.variables.associate { it.key to it.value }
        assertEquals("foo", vars["VAR1"])
    }

    fun testLoadsFromConfiguredDirectory() {
        val base = project.basePath!!
        val dir = File(base, "config").apply { mkdirs() }
        File(dir, ".env").writeText("VAR2=bar\n")

        val service = EnvFileService.getInstance(project)
        service.state.envPath = dir.absolutePath
        service.update()

        val vars = service.state.variables.associate { it.key to it.value }
        assertEquals("bar", vars["VAR2"])
    }

    fun testLoadsFromConfiguredFile() {
        val base = project.basePath!!
        val dir = File(base, "config2").apply { mkdirs() }
        val custom = File(dir, "custom.env").apply { writeText("VAR3=baz\n") }

        val service = EnvFileService.getInstance(project)
        service.state.envPath = custom.absolutePath
        service.update()

        val vars = service.state.variables.associate { it.key to it.value }
        assertEquals("baz", vars["VAR3"])
    }

    fun testVariablesClearedOnUpdate() {
        val base = project.basePath!!
        val envFile = File(base, ".env")
        envFile.writeText("OLD=one\n")

        val service = EnvFileService.getInstance(project)
        service.state.envPath = null
        service.update()
        var vars = service.state.variables.associate { it.key to it.value }
        assertEquals("one", vars["OLD"]) // first load

        // Overwrite .env with different content
        envFile.writeText("NEW=two\n")
        service.update()
        vars = service.state.variables.associate { it.key to it.value }
        assertNull(vars["OLD"]) // should be cleared
        assertEquals("two", vars["NEW"]) // new value present
    }

    fun testExpandEnvironmentVariablesAndCycles() {
        val listener = EnvRunConfigurationListener()
        val method = EnvRunConfigurationListener::class.java.getDeclaredMethod(
            "expandEnvironmentVariables",
            Map::class.java
        )
        method.isAccessible = true

        val input = mapOf(
            "A" to "foo",
            "B" to "${'$'}{A}",
            "C" to "${'$'}{B}",
            "D" to "${'$'}{E}",
            "E" to "${'$'}{D}"
        )

        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(listener, input) as MutableMap<String, String>

        assertEquals("foo", result["A"]) // unchanged
        assertEquals("foo", result["B"]) // expands from A
        assertEquals("foo", result["C"]) // expands transitively
        // cycle should not infinite-loop and should leave at least one unresolved placeholder
        assertEquals("${'$'}{E}", result["D"]) 
        assertEquals("${'$'}{D}", result["E"]) 
    }
}
