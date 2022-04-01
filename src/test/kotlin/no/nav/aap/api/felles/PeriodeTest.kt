package no.nav.aap.api.felles

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.oppslag.krr.KontaktinformasjonDTO
import no.nav.aap.api.oppslag.krr.Målform.EN
import no.nav.aap.util.LoggerUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.time.LocalDate


class PeriodeTest {
val log = LoggerUtil.getLogger(PeriodeTest::class.java)
   // @Test
    fun `varighet viser riktige dager`() {
        val now = LocalDate.now()
        val then = now.plusDays(5)
        val periode = Periode(now, then)

        assertEquals(5, periode.varighetDager)
    }
    //@Test
    fun map() {
        val o = ObjectMapper()
        val deser = "{\"spraak\":\"EN\",\"reservert\":null,\"kanVarsles\":false,\"epostadresse\":null,\"mobiltelefonnummer\":null}\n"
        var k = KontaktinformasjonDTO(EN)
        var x = o.readValue(deser,KontaktinformasjonDTO::class.java)
        println(o.writeValueAsString(k))
    }
    @Test
    fun ctx(){
        val key = "message"
        val r = Mono.just("Hello").subscribeOn(Schedulers.boundedElastic())
            .log()
            .flatMap { s ->
                Mono.deferContextual { ctx ->
                    Mono.just(s + " " + ctx.get(key)).log()
                }.log()
            }
            .log()
            .contextWrite { ctx ->
                ctx.put(key, "World")
            }.log()

        StepVerifier.create(r)
            .expectNext("Hello World")
            .verifyComplete()

    }

}