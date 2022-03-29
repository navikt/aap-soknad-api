package no.nav.aap.api.oppslag

import kotlinx.coroutines.ThreadContextElement
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class TokenValidationThreadContextElement(val holder: TokenValidationContextHolder) : ThreadContextElement<TokenValidationContext>, AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<TokenValidationThreadContextElement>

    private val log = LoggerUtil.getLogger(javaClass)

    override fun updateThreadContext(ctx: CoroutineContext): TokenValidationContext {
        log.info("COROUTINE UPDATE THREAD CTX in thread ${Thread.currentThread().name}")
        val oldState = holder.tokenValidationContext
        holder.tokenValidationContext = ctx[Key]?.holder?.tokenValidationContext
        return oldState
    }

    override fun restoreThreadContext(ctx: CoroutineContext, oldState: TokenValidationContext) {
        log.info("COROUTINE RESTORE THREAD CTX in thread ${Thread.currentThread().name}")
        holder.tokenValidationContext = oldState
    }

}