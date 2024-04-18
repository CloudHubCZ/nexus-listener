package cz.cloudhub.nexuslistener

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "nexus")
data class NexusProperties(
    var nexusUrl: String = "",
    var nexusUrlSecured: String = "",
    var username: String = "",
    var password: String = ""
)