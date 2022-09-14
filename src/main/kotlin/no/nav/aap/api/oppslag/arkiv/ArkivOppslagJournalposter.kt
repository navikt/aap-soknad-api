package no.nav.aap.api.oppslag.arkiv

import java.time.LocalDateTime

    data class ArkivOppslagJournalposter(val journalposter: List<ArkivOppslagJournalpost>) {

        data class ArkivOppslagJournalpost(val journalpostId: String,
                                           val journalposttype: ArkivOppslagJournalpostType,
                                           val journalstatus: ArkivOppslagJournalStatus,
                                           val tittel: String?,
                                           val relevanteDatoer: List<ArkivOppslagRelevantDato>,
                                           val sak: ArkivOppslagSak?,
                                           val dokumenter: List<ArkivOppslagDokumentInfo>) {

            enum class ArkivOppslagJournalpostType {
                I,U,N
            }

            enum class ArkivOppslagJournalStatus {
                MOTTATT,
                JOURNALFOERT
                ,EKSPEDERT,
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

            data class ArkivOppslagSak(val fagsakId: String?, val fahsaksystem: String?, val sakstype: ArkivOppslagSakstype) {

                enum class ArkivOppslagSakstype  {
                    GENERELL_SAK,FAGSAK
                }
            }

            data class ArkivOppslagDokumentInfo(val dokumentInfoId: String, val brevkode: String?, val tittel: String?, val dokumentvarianter: List<ArkivOppslagDokumentVariant>){

                data class ArkivOppslagDokumentVariant(val variantformat: ArkivOppslagDokumentVariantFormat, val filtype: ArkivOppslagDokumentFiltype, val brukerHarTilgang: Boolean) {

                    enum class ArkivOppslagDokumentVariantFormat {
                        ARKIV,SLADDET
                    }

                    enum class ArkivOppslagDokumentFiltype {
                        PDF,JPG,PNG
                    }
                }
            }
        }

    }