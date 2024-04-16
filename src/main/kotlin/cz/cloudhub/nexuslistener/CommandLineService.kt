package cz.cloudhub.nexuslistener

import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Service
class CommandLineService {

    @Throws(IOException::class, InterruptedException::class)
    fun runCommand(commands: List<String>): String {
        val processBuilder = ProcessBuilder(commands)
        processBuilder.redirectErrorStream(true) // Merging the error stream with the output stream

        val process = processBuilder.start()

        val output = StringBuilder()
        process.inputStream.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
        }

        val exitVal = process.waitFor()
        if (exitVal == 0) {
            return output.toString()
        } else {
            throw RuntimeException("Failed to execute command with error: $output")
        }
    }

}