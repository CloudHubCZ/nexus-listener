package cz.cloudhub.nexuslistener

import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


@Service
class SkopeoService {


    fun copyImage(sourceImage: String, destinationImage: String): String? {
        val processBuilder = ProcessBuilder()
        processBuilder.command("bash", "-c", "skopeo copy $sourceImage $destinationImage")
        return try {
            val process = processBuilder.start()
            val output = StringBuilder()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String
            while (reader.readLine().also { line = it } != null) {
                output.append(line + "\n")
            }
            val exitVal = process.waitFor()
            if (exitVal == 0) {
                output.toString()
            } else {
                // Handle the case where Skopeo exits with a non-zero status
                "An error occurred"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "Error during the Skopeo operation"
        } catch (e: InterruptedException) {
            e.printStackTrace()
            "Error during the Skopeo operation"
        }
    }
}