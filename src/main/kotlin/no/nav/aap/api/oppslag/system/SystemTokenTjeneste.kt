package no.nav.aap.api.oppslag.system

import no.nav.aap.health.Pingable
import no.nav.aap.util.StringExtensions.asBearer


interface SystemTokenTjeneste  {
    fun getSystemToken(): SystemToken
    fun bearerToken() = getSystemToken().token.asBearer()
}