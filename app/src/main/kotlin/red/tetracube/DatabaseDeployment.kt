package red.tetracube

import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import red.tetracube.ApplicationGlobal.GREEN_BOLD
import red.tetracube.ApplicationGlobal.RED_BOLD
import red.tetracube.ApplicationGlobal.RESET
import red.tetracube.ApplicationGlobal.YELLOW_BOLD
import java.nio.file.Path

class DatabaseDeployment(
    private val appsApi: AppsV1Api,
    private val coreApi: CoreV1Api,
    private val namespaceName: String,
    private val databasePassword: String,
    private val storagePaths: Map<PathType, Path>
) {

    init {
        createConfigMap()
        createSecret()
        createDbDataPersistentVolume()
        createDbDockerEntrypointPersistentVolume()
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
                "smart-igloo-db-config",
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
                "smart-igloo-db-secrets",
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

    private fun createDbDataPersistentVolume() {
            println("Creating DB persistent volume")
            val persistentVolume = V1PersistentVolume()
                .apiVersion("v1")
                .metadata(
                    V1ObjectMeta()
                        .name("data-db-pv")
                        .labels(
                            mapOf(
                                Pair("type", "local"),
                                Pair("app", "smart-igloo-database")
                            )
                        )
                )
                .spec(
                    V1PersistentVolumeSpec()
                        .storageClassName("manual")
                        .capacity(
                            mapOf(
                                Pair("storage", Quantity.fromString("5Gi"))
                            )
                        )
                        .accessModes(listOf("ReadWriteMany"))
                        .hostPath(
                            V1HostPathVolumeSource()
                                .path(storagePaths[PathType.DATA_DB_PATH].toString())
                        )
                )

        try {
            coreApi.createPersistentVolume(
                persistentVolume,
                "data-db-pv",
                null,
                null,
                null
            )
            println("${GREEN_BOLD}Persistent volume created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Data persistent volume exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }

    private fun createDbDockerEntrypointPersistentVolume() {
        println("Creating DB Docker Entrypoint persistent volume")
        val persistentVolume = V1PersistentVolume()
            .apiVersion("v1")
            .metadata(
                V1ObjectMeta()
                    .name("docker-entrypoint-db-pv")
                    .labels(
                        mapOf(
                            Pair("type", "local"),
                            Pair("app", "smart-igloo-database")
                        )
                    )
            )
            .spec(
                V1PersistentVolumeSpec()
                    .storageClassName("manual")
                    .capacity(
                        mapOf(
                            Pair("storage", Quantity.fromString("10Mi"))
                        )
                    )
                    .accessModes(listOf("ReadOnlyMany"))
                    .hostPath(
                        V1HostPathVolumeSource()
                            .path(storagePaths[PathType.DOCKER_ENTRYPOINT].toString())
                    )
            )

        try {
            coreApi.createPersistentVolume(
                persistentVolume,
                "data-docker-entrypoint-pv",
                null,
                null,
                null
            )
            println("${GREEN_BOLD}Persistent volume created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Data persistent volume exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }
}