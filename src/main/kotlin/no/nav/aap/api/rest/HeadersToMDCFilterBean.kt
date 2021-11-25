package no.nav.aap.api.rest

import no.nav.aap.api.util.CallIdGenerator
import no.nav.aap.api.util.LoggerUtil.getLogger
import no.nav.aap.api.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.api.util.MDCUtil.NAV_CONSUMER_ID
import no.nav.aap.api.util.MDCUtil.toMDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


@Component
@Order(LOWEST_PRECEDENCE)
class HeadersToMDCFilterBean constructor(val generator: CallIdGenerator, @Value("\${spring.application.name}") val applicationName: String) : GenericFilterBean() {
    private val log = getLogger(javaClass)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        putValues(HttpServletRequest::class.java.cast(request))
        chain.doFilter(request, response)
    }

    private fun putValues(req: HttpServletRequest) {
        try {
            toMDC(NAV_CONSUMER_ID, req.getHeader(NAV_CONSUMER_ID), applicationName)
            toMDC(NAV_CALL_ID, req.getHeader(NAV_CALL_ID), generator.create())
        } catch (e: Exception) {
            log.warn("Feil ved setting av MDC-verdier for {}, MDC-verdier er inkomplette", req.requestURI, e)
        }
    }

    override fun toString() = javaClass.simpleName + " [generator=" + generator + ", applicationName=" + applicationName + "]"
}