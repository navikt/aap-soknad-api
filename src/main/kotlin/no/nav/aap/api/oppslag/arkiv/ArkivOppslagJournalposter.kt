package no.nav.aap.api.oppslag.arkiv

import java.time.LocalDateTime
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentFiltype.PDF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagJournalposter.ArkivOppslagJournalpost.ArkivOppslagDokumentInfo.ArkivOppslagDokumentVariant.ArkivOppslagDokumentVariantFormat.ARKIV

data class ArkivOppslagJournalposter(val journalposter: List<ArkivOppslagJournalpost> = emptyList()) {

    data class ArkivOppslagJournalpost(val journalpostId: String,
                                       val journalposttype: ArkivOppslagJournalpostType,
                                       val journalstatus: ArkivOppslagJournalStatus,
                                       val tittel: String?,
                                       val eksternReferanseId: String?,
                                       val relevanteDatoer: List<ArkivOppslagRelevantDato> = emptyList(),
                                       val sak: ArkivOppslagSak?,
                                       val dokumenter: List<ArkivOppslagDokumentInfo> = emptyList()) {

        enum class ArkivOppslagJournalpostType { I, U, N }

        enum class ArkivOppslagJournalStatus {
            MOTTATT,
            JOURNALFOERT,
            EKSPEDERT,
            FERDIGSTILT,
            UNDER_ARBEID,
            FEILREGISTRERT,
            UTGAAR,
            AVBRUTT,
            UKJENT_BRUKER,
            RESERVERT,
            OPPLASTING_DOKUMENT,
            UKJENT
        }

        data class ArkivOppslagRelevantDato(val dato: LocalDateTime, val datotype: ArkivOppslagDatoType) {

            enum class ArkivOppslagDatoType {
                DATO_OPPRETTET,
                DATO_SENDT_PRINT,
                DATO_EKSPEDERT,
                DATO_JOURNALFOERT,
                DATO_REGISTRERT,
                DATO_AVS_RETUR,
                DATO_DOKUMENT
            }
        }

        data class ArkivOppslagSak(val fagsakId: String?,
                                   val fagsaksystem: String?,
                                   val sakstype: ArkivOppslagSakstype) {

            enum class ArkivOppslagSakstype { GENERELL_SAK, FAGSAK }
        }

        data class ArkivOppslagDokumentInfo(val dokumentInfoId: String,
                                            val brevkode: String?,
                                            val tittel: String?,
                                            val dokumentvarianter: List<ArkivOppslagDokumentVariant>) {

            data class ArkivOppslagDokumentVariant(val variantformat: ArkivOppslagDokumentVariantFormat,
                                                   val filtype: ArkivOppslagDokumentFiltype,
                                                   val code: List<String> = emptyList(),
                                                   val brukerHarTilgang: Boolean) {

                val kanVises =  filtype == PDF && brukerHarTilgang && ARKIV == variantformat

                enum class ArkivOppslagDokumentVariantFormat { ARKIV, SLADDET }

                enum class ArkivOppslagDokumentFiltype { PDF, JPG, PNG }
            }
        }
    }
}