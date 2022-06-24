package red.tetracube

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1ObjectMeta
import red.tetracube.ApplicationGlobal.GREEN_BOLD
import red.tetracube.ApplicationGlobal.RED_BOLD
import red.tetracube.ApplicationGlobal.RESET
import red.tetracube.ApplicationGlobal.YELLOW_BOLD

class Namespace(private val coreApi: CoreV1Api) {

    fun createNamespace(namespaceName: String) {
        println("Creating namespace")
        val namespace = V1Namespace()
            .metadata(
                V1ObjectMeta()
                    .name(namespaceName)
            )
        try {
            coreApi.createNamespace(namespace, null, null, null, null)
            println("${GREEN_BOLD}Namespace created${RESET}")
        } catch (exception: ApiException) {
            if (exception.code == 409) {
                println("${YELLOW_BOLD}Namespace already exists${RESET}")
            } else {
                println("${RED_BOLD}${exception.responseBody}${RESET}")
            }
        }
    }

}