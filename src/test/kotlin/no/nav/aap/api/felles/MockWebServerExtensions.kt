package no.nav.aap.api.felles

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

object MockWebServerExtensions {

    private inline operator fun <T> T.invoke(action: T.() -> Unit): T = apply(action)
    fun MockWebServer.expect(body: String?, vararg statuses: HttpStatus) =
        this {
        statuses.iterator().forEach {
            enqueue(MockResponse().setResponseCode(it.value()).apply {
                setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                body?.let { b -> setBody(b) }
            })
        }
    }
     fun MockWebServer.expect(body: String)  = expect(body = body,statuses = arrayOf(OK))
    fun MockWebServer.expect(times: Int,  status: HttpStatus) =
        this  {
            repeat(times) {
                expect(status)
            }
        }
    fun MockWebServer.expect(vararg statuses: HttpStatus) = expect(body = null,statuses = statuses)
}