package jp.kroyeeg.intellijenvfileplugin.run

import com.intellij.execution.configurations.RunConfiguration

/**
 * Sets environment variables on a specific [RunConfiguration] implementation.
 */
interface RunConfigurationEnvSetter {
    /**
     * Applies the provided [env] variables to the given [configuration].
     */
    fun setEnvironment(configuration: RunConfiguration, env: Map<String, String>)
}
