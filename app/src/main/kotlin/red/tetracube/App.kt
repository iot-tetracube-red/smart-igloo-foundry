package red.tetracube

import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import red.tetracube.ApplicationGlobal.GREEN_BOLD
import red.tetracube.ApplicationGlobal.RESET
import red.tetracube.ApplicationGlobal.YELLOW_BOLD
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

class App {

    private val namespaceName = "smart-igloo-hub"
    private val coreApi: CoreV1Api = CoreV1Api()
    private val appsApi: AppsV1Api = AppsV1Api()

    fun getDeploymentProperties() {
        val kubernetesHostnameAddress = getUserInput(
            "${ApplicationGlobal.GREEN} \uD83C\uDFE1 Kubernetes hostname:${ApplicationGlobal.RESET}",
            false,
            null
        )
        val dbPassword = getUserInput(
            "${ApplicationGlobal.BLUE} \uD83D\uDD12 Database default password:${ApplicationGlobal.RESET}",
            true,
            null
        )
        val deploymentFolder = getUserInput(
            "${ApplicationGlobal.BLUE} \uD83D\uDCC2 Deployment folder where persist data:${ApplicationGlobal.RESET}",
            true,
            null
        )
        val environment = getUserInput(
            "${ApplicationGlobal.GREEN} \uD83D\uDC8E Is development or production environment? (dev|prod):${ApplicationGlobal.RESET}",
            false,
            "(dev||prod)"
        )

        val preparedPaths = prepareDiskStorage(deploymentFolder)
        startDeployment(kubernetesHostnameAddress, dbPassword, preparedPaths, environment)
    }

    private fun getUserInput(inputLabel: String, optional: Boolean, regExValidation: String?): String {
        print(inputLabel)
        val input = readLine()!!
        if ((input == "" && !optional) || (regExValidation != null && !regExValidation.toRegex().matches(input))) {
            return getUserInput(inputLabel, false, regExValidation)
        }
        return input
    }

    private fun startDeployment(
        kubernetesHostnameAddress: String,
        databasePassword: String,
        storagePaths: Map<PathType, Path>,
        environment: String
    ) {
        appsApi.apiClient.basePath = kubernetesHostnameAddress
        coreApi.apiClient.basePath = kubernetesHostnameAddress

        Namespace(coreApi).createNamespace(namespaceName)
        DatabaseDeployment(appsApi, coreApi, namespaceName, databasePassword, storagePaths, environment)
    }

    private fun prepareDiskStorage(deploymentFolder: String): Map<PathType, Path> {
        val paths = HashMap<PathType, Path>()

        val originSqlScriptPathString = this.javaClass.getResource("${File.separator}sql")?.path
            ?: throw FileNotFoundException("Cannot find origin directory with init db scripts")

        println("${GREEN_BOLD}Creating base DB Folder${RESET}")
        val baseDeploymentPath =
            try {
                Files.createDirectory(Path("$deploymentFolder${File.separator}smart_igloo_deploy"))
            } catch (exception: java.nio.file.FileAlreadyExistsException) {
                println("${YELLOW_BOLD}Base deployment already exists${RESET}")
                Path("$deploymentFolder${File.separator}smart_igloo_deploy")
            }

        val baseDbPath =
            try {
                Files.createDirectory(Path("${baseDeploymentPath}${File.separator}database"))
            } catch (exception: java.nio.file.FileAlreadyExistsException) {
                println("${YELLOW_BOLD}Database base path already exists${RESET}")
                Path("${baseDeploymentPath}${File.separator}database")
            }

        val dataDbPath =
            try {
                Files.createDirectory(Path("${baseDbPath}${File.separator}data"))
            } catch (exception: java.nio.file.FileAlreadyExistsException) {
                println("${YELLOW_BOLD}Data path already exists${RESET}")
                Path("${baseDbPath}${File.separator}data")
            }
        paths[PathType.DATA_DB_PATH] = dataDbPath

        val entrypointDockerDbPath =
            try {
                Files.createDirectory(Path("${baseDbPath}${File.separator}docker-entrypoint-initdb.d"))
            } catch (exception: java.nio.file.FileAlreadyExistsException) {
                println("${YELLOW_BOLD}Docker entrypoint path already exists${RESET}")
                Path("${baseDbPath}${File.separator}docker-entrypoint-initdb.d")
            }
        paths[PathType.DOCKER_ENTRYPOINT] = entrypointDockerDbPath

        val originSqlScripts = Path(originSqlScriptPathString).listDirectoryEntries()
        println("${GREEN_BOLD}Getting SQL files from $originSqlScripts${RESET}")
        originSqlScripts.forEach { script ->
            val filename = script.fileName
            Files.copy(
                script,
                Path("$entrypointDockerDbPath${File.separator}$filename"),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        return paths
    }
}

fun main() {
    App().getDeploymentProperties()
}
