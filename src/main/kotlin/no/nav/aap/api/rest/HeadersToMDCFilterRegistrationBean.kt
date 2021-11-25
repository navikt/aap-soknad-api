package no.nav.aap.api.rest

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.stereotype.Component


@Component
class HeadersToMDCFilterRegistrationBean(filter: HeadersToMDCFilterBean?) : FilterRegistrationBean<HeadersToMDCFilterBean?>(filter) {
    init {
          urlPatterns = listOf("/*")
    }
}