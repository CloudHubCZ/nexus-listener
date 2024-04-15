package cz.cloudhub.nexuslistener

import ch.qos.logback.core.CoreConstants.STDOUT
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.github.dockerjava.core.command.PullImageResultCallback
import com.github.dockerjava.core.command.WaitContainerResultCallback
import com.github.dockerjava.transport.DockerHttpClient
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class ScanningService(
    val dockerClient: DockerClient
) {

    fun scanImage(imageUrl: String): String? {
        return try {
            // Pull the Trivy image
            dockerClient. pullImageCmd("aquasec/trivy:latest")
                .exec<PullImageResultCallback>(PullImageResultCallback())
                .awaitCompletion()

            // Create and start a container to run Trivy scan
            val container: CreateContainerResponse = dockerClient.createContainerCmd("aquasec/trivy:latest")
                .withCmd("image", "--format", "json", imageUrl)
                .withTty(true)
                .exec()
            dockerClient.startContainerCmd(container.id).exec()

            // Wait for the container to finish running
            val status: Int = dockerClient.waitContainerCmd(container.id)
                .exec<WaitContainerResultCallback>(WaitContainerResultCallback())
                .awaitStatusCode()

            val scanResults = mutableListOf<String>()

            // Fetch the logs (results)
            dockerClient.logContainerCmd(container.id)
                .withStdOut(true)
                .withStdErr(true)
                .exec(object : LogContainerResultCallback() {
                    override fun onNext(item: Frame) {
                        println(item)
                        scanResults.add(item.toString())
                    }
                })
            println(scanResults)
            // Cleanup: stop and remove the container
            dockerClient.removeContainerCmd(container.id)
                .withForce(true)
                .exec()
            return scanResults.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "Error during Trivy scan: " + e.message
        }
    }
}