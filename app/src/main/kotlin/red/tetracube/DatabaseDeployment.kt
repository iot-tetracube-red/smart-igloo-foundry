package red.tetracube

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1ConfigMap
import io.kubernetes.client.openapi.models.V1ObjectMeta
import red.tetracube.ApplicationGlobal.GREEN_BOLD
import red.tetracube.ApplicationGlobal.RESET

class DatabaseDeployment(
    private val appsApi: AppsV1Api,
    private val coreApi: CoreV1Api,
    private val namespaceName: String
) {

    init {
        createConfigMap()
    }

    private fun createConfigMap() {
        val dbUsername = "igloo_nest_db_usr"

        println("Creating DB config map")
        val dbConfig = V1ConfigMap()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-db-config")
            )
            .data(
                mapOf(
                    Pair("db-name", "igloo_nest_db"),
                    Pair("db-username", dbUsername)
                )
            )

        try {
            coreApi.createNamespacedConfigMap(
                namespaceName,
                dbConfig,
                null,
                null,
                null,
                null
            )
            println("${GREEN_BOLD}DB config created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}Database config map already exists${RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }
}