package red.tetracube.iot.cli

import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import picocli.CommandLine
import red.tetracube.iot.cli.deployment.kubernetes.Namespace
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "Smart Igloo Hub CLI Installer",
    description = ["Smart Igloo Hub Platform Installer"],
    mixinStandardHelpOptions = true,
    version = ["1.0.0"]
)
class SmartIglooFoundryCLI : Callable<Int> {

    @CommandLine.Option(names = ["--k8s-host"], description = ["Kubernetes hostname"], defaultValue = "http://localhost:8080")
    private lateinit var kubernetesHostname: String

    private val namespaceName = "smart-igloo-hub"

    private lateinit var coreApi: CoreV1Api
    private lateinit var appsApi: AppsV1Api

    override fun call(): Int {
        this.coreApi = CoreV1Api()
        this.coreApi.apiClient.basePath = kubernetesHostname

        this.appsApi = AppsV1Api()
        this.appsApi.apiClient.basePath = kubernetesHostname

        Namespace(
            namespaceName,
            this.coreApi
        ).createNamespace()

        return 0
    }
}