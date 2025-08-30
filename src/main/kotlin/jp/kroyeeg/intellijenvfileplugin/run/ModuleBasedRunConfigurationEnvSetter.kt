package jp.kroyeeg.intellijenvfileplugin.run

import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.diagnostic.thisLogger

/**
 * Applies environment variables to [ModuleBasedConfiguration] instances via reflection.
 */
class ModuleBasedRunConfigurationEnvSetter : RunConfigurationEnvSetter {
    override fun setEnvironment(configuration: RunConfiguration, env: Map<String, String>) {
        val config = configuration as ModuleBasedConfiguration<*, *>
        runCatching {
            val methods = config.state!!.javaClass.methods
            val getEnvMethod = methods.find { it.name == "getEnv" }
            val setEnvMethod = methods.find { it.name == "setEnv" }
            val newEnv = getEnvMethod?.let { method ->
                method.isAccessible = true
                val envs = method.invoke(config.state) as Map<*, *>
                envs + env
            }
            setEnvMethod?.let { method ->
                method.isAccessible = true
                method.invoke(config.state, newEnv)
            }
        }.onFailure { thisLogger().error(it) }
    }
}
