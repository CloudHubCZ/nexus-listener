package cz.cloudhub.nexuslistener

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.BufferedReader
import java.io.InputStreamReader


@Configuration
class KubernetesApiClientConfig {

    @Bean
    open fun createApiClient(): ApiClient {
        val classLoader = Thread.currentThread().contextClassLoader

        // Open the kubeconfig file from the resources folder
        classLoader.getResourceAsStream("kubeconfig.yaml").use { inputStream ->
            if (inputStream == null) {
                throw RuntimeException("Failed to find kubeconfig file in resources")
            }

            // Create a KubeConfig object from the InputStream
            val kubeConfig = KubeConfig.loadKubeConfig(InputStreamReader(inputStream))

            // Build the ApiClient with the specified kubeconfig
            val client = ClientBuilder.kubeconfig(kubeConfig).build()

            // Set the ApiClient as the default instance
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client)

            return client
        }
    }
}