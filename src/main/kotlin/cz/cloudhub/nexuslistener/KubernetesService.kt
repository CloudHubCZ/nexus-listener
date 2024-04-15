package cz.cloudhub.nexuslistener

import com.google.gson.Gson
import org.springframework.stereotype.Service
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import io.kubernetes.client.util.Watch
import com.google.gson.reflect.TypeToken

@Service
class KubernetesService(
    val kubeApiClient: ApiClient
) {

    fun scanImage(imageName: String): String {
        val api = CoreV1Api(kubeApiClient)
        val currentTimeMillis = System.currentTimeMillis()
        val pod = V1Pod().apply {
            apiVersion = "v1"
            kind = "Pod"
            metadata = createObjectMeta(name = "trivy-scan-${currentTimeMillis}")
            spec = V1PodSpec().apply {
                containers = listOf(V1Container().apply {
                    name = "trivy"
                    image = "aquasec/trivy:latest"
                    command = listOf("trivy", "image","--format", "json", "--insecure", imageName)
                    volumeMounts = listOf(V1VolumeMount().apply {
                        name = "docker-sock"
                        mountPath = "/var/run/docker.sock"
                    })
                    securityContext = V1SecurityContext().apply {
                        privileged = true // Required to access the host's Docker daemon
                        runAsUser = 0L
                    }
                })
                volumes = listOf(V1Volume().apply {
                    name = "docker-sock"
                    hostPath = V1HostPathVolumeSource().apply {
                        path = "/var/run/docker.sock"
                    }
                })
                restartPolicy = "Never"
            }
        }

        val namespace = "nexus"
        api.createNamespacedPod(namespace, pod, null, null, null, null)
        println("HALLOOOOO, looking for metadata.name=${pod.metadata!!.name} on " + pod.metadata!!.name,)
        // Watch for pod to complete
        //"metadata.name=${pod.metadata!!.name}"
        Watch.createWatch<V1Pod>(
            kubeApiClient,
            api.listNamespacedPodCall(
                namespace, null, null, null, null,
                "app=${pod.metadata!!.name}", null, null,
                null, 100, true, null),
            object : TypeToken<Watch.Response<V1Pod>>() {}.type
        ).use { watch ->
            for (event in watch) {
                val eventPod = event.`object`
                if (pod.metadata!!.name == pod.metadata!!.name && (eventPod.status!!.phase!! == "Succeeded"
                    || eventPod.status!!.phase!! == "Failed"
                    || eventPod.status!!.phase!! == "Completed")) {
                    var logs = readLogs(api, namespace, pod.metadata?.name!!)
                    logs = "{" + logs.substringAfter("{")

                    val response = Gson().fromJson(logs, TrivyResponse::class.java)
                    println("----")
                    println(response)
                    println("----")
                    println("Final statistics:")

                    val high = response.Results!!.get(0).Vulnerabilities!!.filter { it.Severity == "HIGH" }
                    val medium = response.Results!!.get(0).Vulnerabilities!!.filter { it.Severity == "MEDIUM" }
                    val low = response.Results!!.get(0).Vulnerabilities!!.filter { it.Severity == "LOW" }
                    println("----")
                    println("HIGH = " + high.count())
                    println("MEDIUM = " + medium.count())
                    println("LOW = " + low.count())

                    return logs
                }
            }
        }

        return "Failed to obtain results"
    }

    private fun readLogs(api: CoreV1Api, namespace: String, podName: String): String {
        return api.readNamespacedPodLog(podName, namespace, null, null, null, null, null, null, null, null, null)
    }

    private fun createObjectMeta(name: String) = V1ObjectMeta().apply {
        this.name = name
        this.labels = mapOf(pair = Pair("app", name))
    }

}