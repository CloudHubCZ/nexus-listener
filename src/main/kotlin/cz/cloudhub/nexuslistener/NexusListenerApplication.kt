package cz.cloudhub.nexuslistener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [NexusProperties::class])
class NexusListenerApplication

fun main(args: Array<String>) {
	runApplication<NexusListenerApplication>(*args)
	println("\n" +
			"   ____ _                 _ _   _       _     \n" +
			"  / ___| | ___  _   _  __| | | | |_   _| |__  \n" +
			" | |   | |/ _ \\| | | |/ _` | |_| | | | | '_ \\ \n" +
			" | |___| | (_) | |_| | (_| |  _  | |_| | |_) |\n" +
			"  \\____|_|\\___/ \\__,_|\\__,_|_| |_|\\__,_|_.__/ \n" +
			"                                              \n")
}
