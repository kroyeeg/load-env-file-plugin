package jp.kroyeeg.intellijenvfileplugin.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.sh.run.ShRunConfiguration

/**
 * Applies environment variables to [ShRunConfiguration] instances (Shell Script).
 */
class ShRunConfigurationEnvSetter : RunConfigurationEnvSetter {
    override fun setEnvironment(configuration: RunConfiguration, env: Map<String, String>) {
        val config = configuration as ShRunConfiguration
        config.envData = config.envData.with(config.envData.envs + env)
    }
}
