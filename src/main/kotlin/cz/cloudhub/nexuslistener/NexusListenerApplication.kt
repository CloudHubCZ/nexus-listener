package cz.cloudhub.nexuslistener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [NexusProperties::class])
class NexusListenerApplication

fun main(args: Array<String>) {
	runApplication<NexusListenerApplication>(*args)
}
