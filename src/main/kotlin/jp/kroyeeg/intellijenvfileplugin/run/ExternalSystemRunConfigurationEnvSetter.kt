package jp.kroyeeg.intellijenvfileplugin.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

/**
 * Applies environment variables to [ExternalSystemRunConfiguration] instances.
 */
class ExternalSystemRunConfigurationEnvSetter : RunConfigurationEnvSetter {
    override fun setEnvironment(configuration: RunConfiguration, env: Map<String, String>) {
        val config = configuration as ExternalSystemRunConfiguration
        val settings = config.settings
        settings.env = settings.env + env
    }
}
