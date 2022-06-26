package red.tetracube

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1ConfigMap
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.openapi.models.V1Secret

class SmartIglooHubDeployment(
    private val appsApi: AppsV1Api,
    private val coreApi: CoreV1Api,
    private val namespaceName: String,
    private val databasePassword: String,
    private val environmentType: String
)  {

    init {
        createConfigMap()
        createSecret()
    }

    private fun createConfigMap() {
        println("Creating Smart Igloo Hub config map")
        val brokerHost = if (environmentType == "dev")
            "localhost"
        else
            "smart-igloo-hub-broker-cip.smart-igloo-hub"
        val dbConfig = V1ConfigMap()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-hub-config")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-hub")
                        )
                    )
            )
            .data(
                mapOf(
                    Pair("db-host", "localhost"),
                    Pair("db-username", "smart_igloo_db_usr"),
                    Pair("db-name", "igloo_hub_db"),
                    Pair("igloo-name", "Igloo Test"),
                    Pair("broker-host", brokerHost),
                    Pair("broker-port", "1883")
                )
            )
        try {
            coreApi.createNamespacedConfigMap(
                namespaceName,
                dbConfig,
                "smart-igloo-hub",
                null,
                null,
                null
            )
            println("${ApplicationGlobal.GREEN_BOLD}Smart Igloo Hub config created${ApplicationGlobal.RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}Smart Igloo Hub config map already exists${ApplicationGlobal.RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${ApplicationGlobal.RESET}")
            }
        }
    }

    private fun createSecret() {
        println("Creating DB secret")
        val dbSecret = V1Secret()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-hub-secrets")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-hub")
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
                "smart-igloo-hub-secrets",
                null,
                null,
                null
            )
            println("${ApplicationGlobal.GREEN_BOLD}Smart Igloo Hub secret created${ApplicationGlobal.RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}Database secret already exists${ApplicationGlobal.RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${ApplicationGlobal.RESET}")
            }
        }
    }
}