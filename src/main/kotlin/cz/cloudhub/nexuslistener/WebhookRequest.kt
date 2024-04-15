package cz.cloudhub.nexuslistener

data class WebhookRequest(
    val timestamp: String?,
    val nodeId: String?,
    val initiator: String?,
    val repositoryName: String?,
    val action: String?,
    val asset: AssetDetails?,
    val component: ComponentDetails?
)

data class ComponentDetails(
    val id: String?,
    val componentId: String?,
    val format: String?,
    val name: String?,
    val version: String?
)

data class AssetDetails(
    val id: String?,
    val assetId: String?,
    val format: String?,
    val name: String?
)