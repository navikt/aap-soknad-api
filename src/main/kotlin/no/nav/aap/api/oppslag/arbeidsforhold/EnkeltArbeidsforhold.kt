package no.nav.aap.api.oppslag.arbeidsforhold

import java.time.LocalDate

data class EnkeltArbeidsforhold(val arbeidsgiverId: String?,
                                val arbeidsgiverIdType: String?,
                                val from: LocalDate,
                                val to: LocalDate?,
                                val stillingsprosent: ProsentAndel?,
                                val arbeidsgiverNavn: String?)