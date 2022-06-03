package red.tetracube.iot.cli.deployment.kubernetes

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1ObjectMeta

class Namespace(
    private val namespaceName: String,
    private val coreApi: CoreV1Api
) {

    fun createNamespace() {
        println("Creating namespace")
        val namespace = V1Namespace()
            .metadata(
                V1ObjectMeta()
                    .name(namespaceName)
            )

        try {
            coreApi.createNamespace(namespace, null, null, null, null)
            println("Namespace created")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("Namespace already exists")
            }
        }
    }
}