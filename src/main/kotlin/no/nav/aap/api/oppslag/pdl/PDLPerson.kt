package no.nav.aap.api.oppslag.pdl

import java.time.LocalDate

internal data class PDLWrappedPerson(val person: Set<PDLPerson>)
internal data class PDLPerson(val fornavn: String?,
                              val mellomnavn: String?,
                              val etternavn: String?,
                              val fødselsdato: LocalDate)