package red.tetracube

import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api


class KubernetesServiceDiscovery {

    fun discoverMQTTService(kubernetesHostnameAddress: String): Map<Int, String> {
        val coreApi = CoreV1Api()
        coreApi.apiClient.basePath = kubernetesHostnameAddress

        val appsApi = AppsV1Api()
        appsApi.apiClient.basePath = kubernetesHostnameAddress

        val services = coreApi.listNamespacedService(
            "smart-igloo-hub",
            null,
            true,
            null,
            null,
            "app=",
            10,
            null,
            null,
            null,
            false
        )

        return services.items.associate { service ->
            services.items.indexOf(service) to "Service ${service.metadata?.name} of type ${service.spec?.type}"
        }
    }
}