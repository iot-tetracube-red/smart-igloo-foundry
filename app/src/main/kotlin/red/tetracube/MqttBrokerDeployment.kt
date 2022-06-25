package red.tetracube

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*

class MqttBrokerDeployment(
    private val appsApi: AppsV1Api,
    private val coreApi: CoreV1Api,
    private val namespaceName: String,
    private val environmentType: String
) {

    init {
        deployMqttBroker()
        exposeClusterIP()
        exposeLoadBalancer()
    }

    private fun deployMqttBroker() {
        println("Deploying MQTT Broker app")
        val deployment = V1Deployment()
            .apiVersion("apps/v1")
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-hub-broker")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-hub-broker")
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
                                    Pair("app", "smart-igloo-hub-broker")
                                )
                            )
                    )
                    .template(
                        V1PodTemplateSpec()
                            .metadata(
                                V1ObjectMeta()
                                    .labels(
                                        mapOf(
                                            Pair("app", "smart-igloo-hub-broker")
                                        )
                                    )
                            )
                            .spec(
                                V1PodSpec()
                                    .containers(
                                        listOf(
                                            V1Container()
                                                .name("smart-igloo-hub-broker")
                                                .image("emqx/emqx-edge")
                                                .imagePullPolicy("IfNotPresent")
                                                .ports(
                                                    listOf(
                                                        V1ContainerPort().containerPort(1883),
                                                        V1ContainerPort().containerPort(18083)
                                                    )
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
                "smart-igloo-hub-broker",
                null,
                null,
                null
            )
            println("${ApplicationGlobal.GREEN_BOLD}Deployment created${ApplicationGlobal.RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}MQTT deployment exists${ApplicationGlobal.RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${ApplicationGlobal.RESET}")
            }
        }
    }

    private fun exposeClusterIP() {
        println("Creating service for MQTT service")
        val clusterIPService = V1Service()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-hub-broker-cip")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-hub-broker")
                        )
                    )
            )
            .spec(
                V1ServiceSpec()
                    .type("ClusterIP")
                    .ports(
                        listOf(
                            V1ServicePort().port(1883)
                        )
                    )
                    .selector(
                        mapOf(
                            Pair("app", "smart-igloo-hub-broker")
                        )
                    )
            )

        try {
            coreApi.createNamespacedService(
                namespaceName,
                clusterIPService,
                "smart-igloo-hub-broker",
                null,
                null,
                null
            )
            println("${ApplicationGlobal.GREEN_BOLD}ClusterIp created${ApplicationGlobal.RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}MQTT ClusterIP exists${ApplicationGlobal.RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${ApplicationGlobal.RESET}")
            }
        }
    }

    private fun exposeLoadBalancer() {
        println("Creating load balancer to access externally to the MQTT service")
        val exposingPort = if (environmentType == "dev")
            listOf(
                V1ServicePort().port(1883).name("mqtt"),
                V1ServicePort().port(18083).name("dashboard")
            )
        else
            listOf(
                V1ServicePort().port(1883).name("mqtt")
            )

        val loadbalancer = V1Service()
            .metadata(
                V1ObjectMeta()
                    .name("smart-igloo-hub-broker-lb")
                    .labels(
                        mapOf(
                            Pair("app", "smart-igloo-hub-broker")
                        )
                    )
            )
            .spec(
                V1ServiceSpec()
                    .type("LoadBalancer")
                    .ports(exposingPort)
                    .selector(
                        mapOf(
                            Pair("app", "smart-igloo-hub-broker")
                        )
                    )
            )

        try {
            coreApi.createNamespacedService(
                namespaceName,
                loadbalancer,
                "smart-igloo-hub-broker",
                null,
                null,
                null
            )
            println("${ApplicationGlobal.GREEN_BOLD}Load balancer created${ApplicationGlobal.RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${ApplicationGlobal.YELLOW_BOLD}Database Loadbalancer exists${ApplicationGlobal.RESET}")
            } else {
                println("${ApplicationGlobal.RED_BOLD}${exception.responseBody}${ApplicationGlobal.RESET}")
            }
        }

    }
}