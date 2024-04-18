package cz.cloudhub.nexuslistener

import com.google.gson.Gson
import org.springframework.stereotype.Service
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import io.kubernetes.client.util.Watch
import com.google.gson.reflect.TypeToken
import mu.KotlinLogging

@Service
class KubernetesService(
    val kubeApiClient: ApiClient,
    val dockerService: DockerService,
    val commandLineService: CommandLineService,
    val nexusProperties: NexusProperties
) {
    private val logger = KotlinLogging.logger {}

    fun scanImage(imageName: String): String {
        val currentTimeMillis = System.currentTimeMillis()
        logger.info("Initializing Trivy scan container, name=" + "trivy-scan-${currentTimeMillis}, scannedImage=$imageName")
        val api = CoreV1Api(kubeApiClient)
        val pod = V1Pod().apply {
            apiVersion = "v1"
            kind = "Pod"
            metadata = createObjectMeta(name = "trivy-scan-${currentTimeMillis}")
            spec = V1PodSpec().apply {
                containers = listOf(V1Container().apply {
                    name = "trivy"
                    image = "aquasec/trivy:latest"
                    command = listOf("trivy", "image", "--format", "json", "--insecure", imageName)
                    //command = listOf("trivy", "image", "--insecure", imageName)
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
        Watch.createWatch<V1Pod>(
            kubeApiClient,
            api.listNamespacedPodCall(
                namespace, null, null, null, null,
                "app=${pod.metadata!!.name}", null, null,
                null, 200, true, null
            ),
            object : TypeToken<Watch.Response<V1Pod>>() {}.type
        ).use { watch ->
            for (event in watch) {
                val eventPod = event.`object`
                if (pod.metadata!!.name == pod.metadata!!.name && (eventPod.status!!.phase!! == "Succeeded"
                            || eventPod.status!!.phase!! == "Failed"
                            || eventPod.status!!.phase!! == "Completed")
                ) {
                    var logs = readLogs(api, namespace, pod.metadata?.name!!)
                    logs = "{" + logs.substringAfter("{")
                    //println(logs)
                    //logger.info("Parsed response:")
                    val response = Gson().fromJson(logs, TrivyResponse::class.java)
                    logger.info("----")

                    val oscritical = response.Results!!.get(0).Vulnerabilities!!.filter { it.Severity == "CRITICAL" }
                    val oshigh = response.Results.get(0).Vulnerabilities!!.filter { it.Severity == "HIGH" }
                    val osmedium = response.Results.get(0).Vulnerabilities!!.filter { it.Severity == "MEDIUM" }
                    val oslow = response.Results.get(0).Vulnerabilities!!.filter { it.Severity == "LOW" }

                    val critical = response.Results.get(1).Vulnerabilities!!.filter { it.Severity == "CRITICAL" }
                    val high = response.Results.get(1).Vulnerabilities!!.filter { it.Severity == "HIGH" }
                    val medium = response.Results.get(1).Vulnerabilities!!.filter { it.Severity == "MEDIUM" }
                    val low = response.Results.get(1).Vulnerabilities!!.filter { it.Severity == "LOW" }

                    logger.info("SCAN RESULTS, image=$imageName:")
                    logger.info("----")
                    logger.info("OS VULNERABILITIES COUNT:")
                    logger.info("CRITICAL = " + oscritical.count() + "  (packages=" + response.Results.get(0)!!.Vulnerabilities!!.filter { it.Severity == "CRITICAL"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("HIGH     = " + oshigh.count()  + "  (packages=" + response.Results.get(0)!!.Vulnerabilities!!.filter { it.Severity == "HIGH"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("MEDIUM   = " + osmedium.count() + "  (packages=" + response.Results.get(0)!!.Vulnerabilities!!.filter { it.Severity == "MEDIUM"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("LOW      = " + oslow.count() + "  (packages=" + response.Results.get(0)!!.Vulnerabilities!!.filter { it.Severity == "LOW"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("----")
                    logger.info("JAVA VULNERABILITIES COUNT:")
                    logger.info("CRITICAL = " + critical.count() + "  (packages=" + response.Results.get(1)!!.Vulnerabilities!!.filter { it.Severity == "CRITICAL"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("HIGH     = " + high.count() + "  (packages=" + response.Results.get(1)!!.Vulnerabilities!!.filter { it.Severity == "HIGH"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("MEDIUM   = " + medium.count() + "  (packages=" + response.Results.get(1)!!.Vulnerabilities!!.filter { it.Severity == "MEDIUM"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("LOW      = " + low.count() + "  (packages=" + response.Results.get(1)!!.Vulnerabilities!!.filter { it.Severity == "LOW"}.map { it.PkgName }.toList().joinToString(separator = ", ")+ ")")
                    logger.info("----")
                    if (critical.count() < 5) {
                        logger.info("There is less then 5 CRITICAL vulnerabilities.. copying to main repository!")
                        val repo = imageName.substringBefore("/")
                        val image = imageName.substringAfter("/").substringBefore(":")
                        val tag = imageName.substringAfterLast(":")
                        //dockerService.pullImage(repo, image, tag)
                        val newImageName = "${nexusProperties.nexusUrlSecured}/${image}:${tag}-scanned"
                        val copyImageCommand =
                            "skopeo copy --src-creds=${nexusProperties.username}:${nexusProperties.password} --dest-creds=${nexusProperties.username}:${nexusProperties.password} --dest-tls-verify=false --src-tls-verify=false docker://${repo}/${image}:${tag} docker://${newImageName}"
                        //logger.info("Running command:")
                        //logger.info(copyImageCommand)
                        commandLineService.runCommand(listOf("sh", "-c", copyImageCommand))
                        logger.info("Successfully done! Saved as $newImageName")
                    }
                    return logs
                } else if (pod.metadata!!.name == pod.metadata!!.name && (eventPod.status!!.phase!! == "Running")) {
                    logger.info("Trivy scan container started, waiting for scan to finish ..")
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