package cz.cloudhub.nexuslistener

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.transport.DockerHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


@Configuration
class DockerConfig(
    val nexusProperties: NexusProperties
) {

    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withRegistryEmail("info@cloudhub.cz")
            .withRegistryPassword(nexusProperties.password)
            .withRegistryUsername(nexusProperties.username)
            .withDockerTlsVerify("0")
            .withDockerHost("unix:///var/run/docker.sock").build()


        val httpClient: DockerHttpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(100)
            .build()

        return DockerClientImpl.getInstance(config, httpClient);
    }
}