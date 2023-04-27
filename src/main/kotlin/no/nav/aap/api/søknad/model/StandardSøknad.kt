package no.nav.aap.api.søknad.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import com.neovisionaries.i18n.CountryCode
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.util.*
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.behandler.AnnenBehandler
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.søknad.model.AnnetBarnOgInntekt.Relasjon.FORELDER
import no.nav.aap.api.søknad.model.RadioValg.JA
import no.nav.aap.api.søknad.model.Studier.StudieSvar.AVBRUTT
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.AFP
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.LÅN
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.OMSORGSSTØNAD
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.STIPEND
import no.nav.aap.api.søknad.model.Utbetalinger.AnnenStønadstype.UTLAND
import no.nav.aap.api.søknad.model.VedleggType.ANDREBARN
import no.nav.aap.api.søknad.model.VedleggType.ANNET
import no.nav.aap.api.søknad.model.VedleggType.ARBEIDSGIVER
import no.nav.aap.api.søknad.model.VedleggType.LÅNEKASSEN_LÅN
import no.nav.aap.api.søknad.model.VedleggType.LÅNEKASSEN_STIPEND
import no.nav.aap.api.søknad.model.VedleggType.OMSORG
import no.nav.aap.api.søknad.model.VedleggType.STUDIER
import no.nav.aap.util.LoggerUtil.getLogger

data class Innsending(
    val søknad: StandardSøknad,
    val kvittering: PDFKvittering) {
    val andreBarn = søknad.andreBarn.map { it.barn }

}



@JsonIgnoreProperties(value= ["vedlegg"], allowSetters = true)
data class StandardSøknad(
        val sykepenger: Boolean,
        val ferie: Ferie?,
        val studier: Studier,
        val medlemsskap: Medlemskap,
        val registrerteBehandlere: List<RegistrertBehandler> = emptyList(),
        val andreBehandlere: List<AnnenBehandler> = emptyList(),
        val yrkesskadeType: Yrkesskade,
        val utbetalinger: Utbetalinger?,
        val tilleggsopplysninger: String?,
        val registrerteBarn: List<BarnOgInntekt> = emptyList(),
        val andreBarn: List<AnnetBarnOgInntekt> = emptyList(),
        override val vedlegg: Vedlegg? = null) : VedleggAware {



    companion object{
        const val VERSJON = "1.0"
    }

    var fødselsdato : LocalDate? = null
    val innsendingTidspunkt = now()

    data class Ferie(val ferietype: FerieType, var periode: Periode?, var dager: String?) {
        enum class FerieType {
            PERIODE, DAGER,NEI
        }
    }

    enum class Yrkesskade { JA, NEI }

    private val log = getLogger(javaClass)

    data class VedleggInfo(val vedlagte: List<VedleggType>, val manglende: List<VedleggType>)

    fun vedlegg(): VedleggInfo {

        val manglende = mutableListOf<VedleggType>()
        val innsendte = mutableListOf<VedleggType>()

        log.trace("Sjekker om det er manglende vedlegg for studier $studier")
        if (studier.erStudent == AVBRUTT && studier.kommeTilbake == JA && manglerVedlegg(studier)) {
            log.trace("Det er manglende vedlegg for ${STUDIER.tittel}").also {
                manglende += STUDIER
            }
        }
        else {
            if (harVedlegg(studier)) {
                log.trace("Studier har vedlegg")
                innsendte += STUDIER
            }
            log.trace("Ingen manglende vedlegg for studier")
        }
        with(andreBarn) {
            log.trace("Sjekker om det er manglende vedlegg for andre barn $andreBarn")
            forEach { a -> log.trace("Annet barn relasjon ${a.relasjon} vedlegg ${a.vedlegg}") }
            if (count() > count { (it.vedlegg?.deler?.size ?: 0) > 0 }) {
                manglende += ANDREBARN.also {
                    log.trace("Det er manglende vedlegg for ${ANDREBARN.tittel}")
                }
            }
            else {
                if (harVedlegg(this)) {
                    log.trace("Barn har vedlegg")
                    innsendte += ANDREBARN
                }
                log.trace("Ingen manglende vedlegg for andre barn")
            }
        }
        with(utbetalinger) {
            log.trace("Sjekker om det er manglende vedlegg for arbeidsgiver ${this?.ekstraFraArbeidsgiver}")
            if (this?.ekstraFraArbeidsgiver?.fraArbeidsgiver == true && manglerVedlegg(ekstraFraArbeidsgiver)) {
                manglende += ARBEIDSGIVER.also {
                    log.trace("Det er manglende vedlegg for ${ARBEIDSGIVER.tittel}")
                }
            }
            else {
                log.trace("Ingen manglende vedlegg for arbeidsgiver")
            }
            log.trace("Sjekker om det er manglende vedlegg for andre stønader ${this?.andreStønader}")
            this?.andreStønader?.firstOrNull { it.type == OMSORGSSTØNAD }?.let {
                if (manglerVedlegg(it)) {
                    manglende += OMSORG.also {
                        log.trace("Det er manglende vedlegg for ${OMSORG.tittel}")
                    }
                }
                else {
                    if (harVedlegg(it)) {
                        log.trace("Omsorg har vedlegg")
                        innsendte += OMSORG
                    }
                    log.trace("Ingen manglende vedlegg for omsorg")
                }
            }
            this?.andreStønader?.firstOrNull { it.type == UTLAND }?.let {
                if (manglerVedlegg(it)) {
                    manglende += VedleggType.UTENLANDSKE.also {
                        log.trace("Det er manglende vedlegg for ${VedleggType.UTENLANDSKE.tittel}")
                    }
                }
                else {
                    if (harVedlegg(it)) {
                        log.trace("Utland har vedlegg")
                        innsendte += VedleggType.UTENLANDSKE
                    }
                    log.trace("Ingen manglende vedlegg for utland")
                }
            }
            this?.andreStønader?.firstOrNull { it.type == LÅN }?.let {
                if (manglerVedlegg(it)) {
                    manglende += LÅNEKASSEN_LÅN.also {
                        log.trace("Det er manglende vedlegg for ${LÅNEKASSEN_LÅN.tittel}")
                    }
                }
                else {
                    if (harVedlegg(it)) {
                        log.trace("Lån fra Lånekassen har vedlegg")
                        innsendte += LÅNEKASSEN_LÅN
                    }
                    log.trace("Ingen manglende vedlegg for lån fra Lånekassen")
                }
            }

            this?.andreStønader?.firstOrNull { it.type == STIPEND }?.let {
                if (manglerVedlegg(it)) {
                    manglende += LÅNEKASSEN_STIPEND.also {
                        log.trace("Det er manglende vedlegg for ${LÅNEKASSEN_STIPEND.tittel}")
                    }
                }
                else {
                    if (harVedlegg(it)) {
                        log.trace("Stipend fra Lånekassen har vedlegg")
                        innsendte += LÅNEKASSEN_STIPEND
                    }
                    log.trace("Ingen manglende vedlegg for stipend fra Lånekassen")
                }
            }

        }
        log.trace("Sjekker om det er andre vedlegg")
        if (vedlegg?.deler?.isNotEmpty() == true) {
            log.trace("Det er andre vedlegg")
            innsendte += ANNET
        }
        else {
            log.trace("Det er ingen andre vedlegg")
        }
        log.trace("Manglende vedlegg er $manglende, innsendte er $innsendte")
        return VedleggInfo(innsendte, manglende)
    }
}

fun manglerVedlegg(v: VedleggAware) = v.vedlegg?.deler?.isEmpty() == true
fun harVedlegg(v: VedleggAware) = !manglerVedlegg(v)
private fun harVedlegg(v: List<VedleggAware>) = v.any { harVedlegg(it) }

@JsonDeserialize(using = VedleggDeserializer::class)
data class Vedlegg(val tittel: String? = null, @JsonValue val deler: List<UUID?>? = null)

interface VedleggAware {
    val vedlegg: Vedlegg?
}

data class Studier(val erStudent: StudieSvar?,
                   val kommeTilbake: RadioValg?,
                   override val vedlegg: Vedlegg? = null) : VedleggAware {

    enum class StudieSvar {
        JA,
        NEI,
        AVBRUTT
    }
}

data class Medlemskap(val boddINorgeSammenhengendeSiste5: Boolean,
                      val jobbetUtenforNorgeFørSyk: Boolean?,
                      val jobbetSammenhengendeINorgeSiste5: Boolean?,
                      val iTilleggArbeidUtenforNorge: Boolean?,
                      val utenlandsopphold: List<Utenlandsopphold> = emptyList())

data class Utenlandsopphold(val land: CountryCode,
                            val periode: Periode,
                            val arbeidet: Boolean,
                            val id: String?) {

    val landnavn = land.toLocale().displayCountry
}

data class BarnOgInntekt(val merEnnIG: Boolean? = false)
@JsonIgnoreProperties(value= ["vedlegg"], allowSetters = true)
data class AnnetBarnOgInntekt(val barn: Barn,
                              val relasjon: Relasjon = FORELDER,
                              val merEnnIG: Boolean? = false,
                              override val vedlegg: Vedlegg? = null) : VedleggAware {

    enum class Relasjon {
        FOSTERFORELDER,
        FORELDER
    }
}

enum class RadioValg {
    JA,
    NEI,
    VET_IKKE
}

data class Utbetalinger(val ekstraFraArbeidsgiver: FraArbeidsgiver,
                        @JsonAlias("stønadstyper") val andreStønader: List<AnnenStønad> = emptyList()) {

    @JsonIgnoreProperties(value= ["vedlegg"], allowSetters = true)
    data class FraArbeidsgiver(val fraArbeidsgiver: Boolean,
                               override val vedlegg: Vedlegg? = null) : VedleggAware

    @JsonIgnoreProperties(value= ["vedlegg"], allowSetters = true)
    data class AnnenStønad(val type: AnnenStønadstype,
                           val hvemUtbetalerAFP: String? = null,
                           override val vedlegg: Vedlegg? = null) : VedleggAware {

        init {
            require((type == AFP && hvemUtbetalerAFP != null) || (type != AFP && hvemUtbetalerAFP == null))
        }
    }

    enum class AnnenStønadstype {
        KVALIFISERINGSSTØNAD,
        ØKONOMISK_SOSIALHJELP,
        INTRODUKSJONSSTØNAD,
        OMSORGSSTØNAD,
        STIPEND,
        LÅN,
        AFP,
        VERV,
        UTLAND,
        ANNET,
        NEI
    }
}

enum class VedleggType(val tittel: String) {
    ARBEIDSGIVER("Dokumentasjon av ekstra utbetaling fra arbeidsgiver"),
    STUDIER("Dokumentasjon av studier"),
    LÅNEKASSEN_STIPEND("Kopi av vedtaket eller søknaden om sykestipend fra Lånekassenn"),
    LÅNEKASSEN_LÅN("Dokumentasjon av studielån fra Lånekassen"),
    ANDREBARN("Dokumentasjon av andre barn"),
    OMSORG("Dokumentasjon av omsorgsstønad fra kommunen"),
    UTENLANDSKE("Dokumentasjon av ytelser fra utenlandske trygdemyndigheter"),
    ANNET("Annen dokumentasjon")
}

internal class VedleggDeserializer : StdDeserializer<Vedlegg>(Vedlegg::class.java) {

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctx: DeserializationContext) =
        with(p.codec.readTree(p) as TreeNode) {
            when (this) {
                is ArrayNode -> Vedlegg(deler = filterNotNull().map { (it as TextNode).textValue() }
                    .map { UUID.fromString(it) })

                is TextNode -> Vedlegg(deler = listOf(UUID.fromString(textValue())))
                else -> throw IllegalStateException("Ikke-forventet node ${javaClass.simpleName}")
            }
        }
}