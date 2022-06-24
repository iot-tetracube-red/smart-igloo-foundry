package red.tetracube

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1ObjectMeta

class Namespace(private val coreApi: CoreV1Api) {

    fun createNamespace(namespaceName: String) {
        println("Creating namespace")
        val namespace = V1Namespace()
            .metadata(
                V1ObjectMeta()
                    .name(namespaceName)
            )
        coreApi.createNamespace(namespace, null, null, null, null)
        println("Namespace created")
    }

}