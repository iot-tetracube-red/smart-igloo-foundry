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
        createDbDataPersistentVolumeClaim()
        createDbDockerEntrypointPersistentVolume()
        createDbDockerEntrypointPersistentVolumeClaim()
        deployDatabaseApplication()
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

    private fun createDbDataPersistentVolumeClaim() {
        println("Creating DB persistent volume claim")
        val persistentVolumeClaim = V1PersistentVolumeClaim()
            .apiVersion("v1")
            .metadata(
                V1ObjectMeta()
                    .name("data-db-pvc")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-database")
                        )
                    )
            )
            .spec(
                V1PersistentVolumeClaimSpec()
                    .storageClassName("manual")
                    .accessModes(listOf("ReadWriteMany"))
                    .resources(
                        V1ResourceRequirements()
                            .requests(
                                mapOf(
                                    Pair("storage", Quantity.fromString("5Gi"))
                                )
                            )
                    )
            )

        try {
            coreApi.createNamespacedPersistentVolumeClaim(
                namespaceName,
                persistentVolumeClaim,
                "smart-igloo-database",
                null,
                null,
                null
            )
            println("${GREEN_BOLD}Persistent volume claim created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Data persistent volume claim exists${RESET}")
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
                "docker-entrypoint-db-pv",
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

    private fun createDbDockerEntrypointPersistentVolumeClaim() {
        println("Creating init DB persistent volume claim")
        val persistentVolumeClaim = V1PersistentVolumeClaim()
            .apiVersion("v1")
            .metadata(
                V1ObjectMeta()
                    .name("docker-entrypoint-db-pvc")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-database")
                        )
                    )
            )
            .spec(
                V1PersistentVolumeClaimSpec()
                    .storageClassName("manual")
                    .accessModes(
                        listOf("ReadOnlyMany")
                    )
                    .resources(
                        V1ResourceRequirements()
                            .requests(
                                mapOf(
                                    Pair("storage", Quantity.fromString("10Mi"))
                                )
                            )
                    )
            )

        try {
            coreApi.createNamespacedPersistentVolumeClaim(
                namespaceName,
                persistentVolumeClaim,
                "docker-entrypoint-db-pvc",
                null,
                null,
                null
            )
            println("Persistent volume claim created")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Data persistent volume claim exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }

    private fun deployDatabaseApplication() {
        println("Deploying DB app")
        val deployment = V1Deployment()
            .apiVersion("apps/v1")
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-database")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-database")
                        )
                    )
            )
            .spec(
                V1DeploymentSpec()
                    .replicas(1)
                    .selector(
                        V1LabelSelector()
                            .matchLabels(
                                mapOf(
                                    Pair("app", "smart-igloo-database")
                                )
                            )
                    )
                    .template(
                        V1PodTemplateSpec()
                            .metadata(
                                V1ObjectMeta()
                                    .labels(
                                        mapOf(
                                            Pair("app", "smart-igloo-database")
                                        )
                                    )
                            )
                            .spec(
                                V1PodSpec()
                                    .containers(
                                        listOf(
                                            V1Container()
                                                .name("smart-igloo-database")
                                                .image("postgres")
                                                .imagePullPolicy("IfNotPresent")
                                                .ports(
                                                    listOf(
                                                        V1ContainerPort()
                                                            .containerPort(5432)
                                                    )
                                                )
                                                .env(
                                                    listOf(
                                                        V1EnvVar()
                                                            .name("POSTGRES_DB")
                                                            .valueFrom(
                                                                V1EnvVarSource()
                                                                    .configMapKeyRef(
                                                                        V1ConfigMapKeySelector()
                                                                            .name("smart-igloo-db-config")
                                                                            .optional(false)
                                                                            .key("db-name")
                                                                    )
                                                            ),
                                                        V1EnvVar()
                                                            .name("POSTGRES_USER")
                                                            .valueFrom(
                                                                V1EnvVarSource()
                                                                    .configMapKeyRef(
                                                                        V1ConfigMapKeySelector()
                                                                            .name("smart-igloo-db-config")
                                                                            .optional(false)
                                                                            .key("db-username")
                                                                    )
                                                            ),
                                                        V1EnvVar()
                                                            .name("POSTGRES_PASSWORD")
                                                            .valueFrom(
                                                                V1EnvVarSource()
                                                                    .secretKeyRef(
                                                                        V1SecretKeySelector()
                                                                            .name("smart-igloo-db-secrets")
                                                                            .optional(false)
                                                                            .key("db-password")
                                                                    )
                                                            )
                                                    )
                                                )
                                                .volumeMounts(
                                                    listOf(
                                                        V1VolumeMount()
                                                            .mountPath("/var/lib/postgresql/data")
                                                            .name("data-volume"),
                                                        V1VolumeMount()
                                                            .mountPath("/docker-entrypoint-initdb.d")
                                                            .name("docker-entrypoint-volume")
                                                    )
                                                )
                                        )
                                    )
                                    .volumes(
                                        listOf(
                                            V1Volume()
                                                .name("docker-entrypoint-volume")
                                                .persistentVolumeClaim(
                                                    V1PersistentVolumeClaimVolumeSource()
                                                        .claimName("docker-entrypoint-db-pvc")
                                                ),
                                            V1Volume()
                                                .name("data-volume")
                                                .persistentVolumeClaim(
                                                    V1PersistentVolumeClaimVolumeSource()
                                                        .claimName("data-db-pvc")
                                                )
                                        )
                                    )
                            )
                    )
            )

        try {
            appsApi.createNamespacedDeployment(
                namespaceName,
                deployment,
                "smart-igloo-database",
                null,
                null,
                null
            )
            println("Deployment created")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Database deployment exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }

    }
}