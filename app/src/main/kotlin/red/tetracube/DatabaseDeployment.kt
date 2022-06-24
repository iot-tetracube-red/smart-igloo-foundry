package red.tetracube

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1ConfigMap
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.openapi.models.V1Secret
import red.tetracube.ApplicationGlobal.GREEN_BOLD
import red.tetracube.ApplicationGlobal.RED_BOLD
import red.tetracube.ApplicationGlobal.RESET
import red.tetracube.ApplicationGlobal.YELLOW_BOLD

class DatabaseDeployment(
    private val appsApi: AppsV1Api,
    private val coreApi: CoreV1Api,
    private val namespaceName: String,
    private val databasePassword: String
) {

    init {
        createConfigMap()
        createSecret()
    }

    private fun createConfigMap() {
        val dbUsername = "igloo_nest_db_usr"

        println("Creating DB config map")
        val dbConfig = V1ConfigMap()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-db-config")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-database")
                        )
                    )
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
                println("${YELLOW_BOLD}Database config map already exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }

    private fun createSecret() {
        println("Creating DB secret")
        val dbSecret = V1Secret()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-db-secrets")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-database")
                        )
                    )
            )
            .data(
                mapOf(
                    Pair("db-password", databasePassword.encodeToByteArray())
                )
            )
        try {
            coreApi.createNamespacedSecret(
                namespaceName,
                dbSecret,
                null,
                null,
                null,
                null
            )
            println("${GREEN_BOLD}DB secret created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Database secret already exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }
}