package no.nav.aap.api.config

import no.nav.boot.conditionals.EnvUtil
import org.springframework.core.env.Environment

class EnvExtensions {
    companion object {
        fun Environment.isProd() = EnvUtil.isProd(this)
        fun Environment.isDev() = EnvUtil.isDev(this)
    }
}