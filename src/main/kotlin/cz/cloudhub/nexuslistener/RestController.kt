package cz.cloudhub.nexuslistener

import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController(
    val scanningService: ScanningService,
    val nexusProperties: NexusProperties,
    val kubernetesService: KubernetesService
) {

    private val logger = KotlinLogging.logger {}
    @PostMapping("/webhook")
    fun processWebhook(@RequestBody request: WebhookRequest): String {
        if (request.component != null && request.action == "CREATED") logger.info("Webhook request with CREATE received, request=$request")
        return if (request.component != null && request.action == "CREATED" && !request.component.version!!.contains("scanned") && request.component.format == "docker") {
            val imageName = "${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}"
            logger.info("New image push detected, proceeding to scan, image=$imageName")
            val result =
                //scanningService.scanImage("${nexusProperties.nexusUrl}/${request.component!!.name}:${request.component.version}")
                kubernetesService.scanImage(imageName)
            result
        } else {
            "bad request"
        }
    }

}