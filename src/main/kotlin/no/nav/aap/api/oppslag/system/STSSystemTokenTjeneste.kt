package no.nav.aap.api.oppslag.system

import org.springframework.stereotype.Service

@Service
class STSSystemTokenTjeneste(private val adapter: STSWebClientAdapter) : SystemTokenTjeneste {
    private var systemToken: SystemToken? = null


    override fun getSystemToken(): SystemToken {
        if (systemToken == null || systemToken!!.isExpired(adapter.slack)) {
            systemToken = adapter.refresh()
        }
        return systemToken!!
    }
    override fun toString() = "${javaClass.simpleName} [adapter= $adapter, systemToken=$systemToken]"
}