package no.nav.aap.api.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.stereotype.Component


@Component
class HeadersToMDCFilterRegistrationBean(headersFilter: HeadersToMDCFilterBean?) : FilterRegistrationBean<HeadersToMDCFilterBean?>() {
    init {
        filter = headersFilter
        urlPatterns =listOf(ALWAYS)
        LOG.info("Registrert filter {}", this.javaClass.simpleName)
    }
    companion object {
        private const val ALWAYS = "/*"
        private val LOG: Logger = LoggerFactory.getLogger(HeadersToMDCFilterRegistrationBean::class.java)
    }
}