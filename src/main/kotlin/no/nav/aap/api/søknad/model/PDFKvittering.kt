package no.nav.aap.api.søknad.model

import java.time.LocalDateTime

data class PDFKvittering(val temaer : List<Tema>, val mottattdato : LocalDateTime = LocalDateTime.now()) {
    data class Tema(val type : String, val overskrift : String?, val underblokker : List<Blokk>) {
        data class Blokk(val type : String,
                         val overskrift : String?,
                         val tabellrader : List<Blokk>?,
                         val tekst : String?,
                         val tittel : String?,
                         val punkter : List<String>?,
                         val felt : String?,
                         val felter : List<Blokk>?,
                         val indent : Boolean?,
                         val verdi : String?)
    }
}