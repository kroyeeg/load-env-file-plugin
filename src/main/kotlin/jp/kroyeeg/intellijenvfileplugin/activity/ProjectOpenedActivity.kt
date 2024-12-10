package jp.kroyeeg.intellijenvfileplugin.activity

import jp.kroyeeg.intellijenvfileplugin.services.DotEnvFilePluginService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.github.cdimascio.dotenv.Dotenv
import java.io.File

internal class ProjectOpenedActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (File(project.basePath + "/.env").isFile) {
            val dotenv = Dotenv.configure().directory(project.basePath)
                .filename(".env")
                .load()
            DotEnvFilePluginService.getInstance(project).update(
                dotenv.entries(io.github.cdimascio.dotenv.Dotenv.Filter.DECLARED_IN_ENV_FILE)
            )
        }
    }
}
