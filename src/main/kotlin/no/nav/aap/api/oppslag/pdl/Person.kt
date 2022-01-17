package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.felles.Navn
import java.time.LocalDate

data class Person(val navn: Navn, val fødseldato: LocalDate)