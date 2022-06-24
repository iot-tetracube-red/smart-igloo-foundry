package red.tetracube

import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api

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
            false,
            null
        )
        val deploymentFolder = getUserInput(
            "${ApplicationGlobal.BLUE} \uD83D\uDCC2 Deployment folder where persist data:${ApplicationGlobal.RESET}",
            true,
            null
        )
        val deploymentId = getUserInput(
            "${ApplicationGlobal.GREEN} \uD83D\uDC8E Is development or production environment? (dev|prod):${ApplicationGlobal.RESET}",
            false,
            "(dev||prod)"
        )

        startDeployment(kubernetesHostnameAddress, dbPassword)
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
        databasePassword: String
    ) {
        appsApi.apiClient.basePath = kubernetesHostnameAddress
        coreApi.apiClient.basePath = kubernetesHostnameAddress

        Namespace(coreApi).createNamespace(namespaceName)
        DatabaseDeployment(appsApi, coreApi, namespaceName, databasePassword)
    }
}

fun main() {
    App().getDeploymentProperties()
}
