package cz.cloudhub.nexuslistener

data class TrivyResponse(
    val Results: List<TriveResult>?
)

data class TriveResult(
    val Vulnerabilities: List<Vulnerability>?
)

data class Vulnerability(
    val VulnerabilityID: String?,
    val Status: String?,
    val PkgName: String?,
    val Severity: String?,
    val Title: String?
)