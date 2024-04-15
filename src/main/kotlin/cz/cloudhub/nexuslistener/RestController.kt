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
        if (request.component != null && request.action == "CREATED" && request.component.format == "docker") {
            val imageName = "${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}"
            val result =
                //scanningService.scanImage("${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}")
                kubernetesService.scanImage(imageName)
            return result!!
        } else {
            return "bad request"
        }
    }

}