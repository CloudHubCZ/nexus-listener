package cz.cloudhub.nexuslistener

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController(
    val scanningService: ScanningService,
    val nexusProperties: NexusProperties,
    val kubernetesService: KubernetesService
) {

    @PostMapping("/webhook")
    fun processWebhook(@RequestBody request: WebhookRequest): String {
        return if (request.component != null && request.action == "CREATED" && !request.component.version!!.contains("scanned") && request.component.format == "docker") {
            println("Webhook received, request=" + request)
            val imageName = "${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}"
            println("New image push detected, image=" + imageName)
            val result =
                //scanningService.scanImage("${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}")
                kubernetesService.scanImage(imageName)
            result!!
        } else {
            "bad request"
        }
    }

}