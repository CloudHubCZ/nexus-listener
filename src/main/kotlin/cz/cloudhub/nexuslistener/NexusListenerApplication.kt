package cz.cloudhub.nexuslistener

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [NexusProperties::class])
class NexusListenerApplication

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
	runApplication<NexusListenerApplication>(*args)
	logger.info("\n" +
			"   ____ _                 _ _   _       _     \n" +
			"  / ___| | ___  _   _  __| | | | |_   _| |__  \n" +
			" | |   | |/ _ \\| | | |/ _` | |_| | | | | '_ \\ \n" +
			" | |___| | (_) | |_| | (_| |  _  | |_| | |_) |\n" +
			"  \\____|_|\\___/ \\__,_|\\__,_|_| |_|\\__,_|_.__/ \n" +
			"                                              \n")
}
