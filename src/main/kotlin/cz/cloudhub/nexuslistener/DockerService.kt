package cz.cloudhub.nexuslistener

import com.github.dockerjava.api.DockerClient
import org.springframework.stereotype.Service

@Service
class DockerService(
    val dockerClient: DockerClient
) {

    fun pullImage(repository: String, imageName: String, tag: String) {
        val fullImageName = "$repository/$imageName:$tag"
        val newImageName = "$repository/$imageName"
        println("Pulling old image $fullImageName")
        dockerClient.pullImageCmd(fullImageName).start().awaitCompletion()

        val newTag: String = ("$repository/$imageName").toString() + ":" + tag
        println("Tagging new image to $newImageName")
        dockerClient.tagImageCmd(fullImageName, newImageName, "newtag").exec();
        println("Pushing new image $newImageName")
        dockerClient.pushImageCmd(newTag).start().awaitCompletion();
    }

}