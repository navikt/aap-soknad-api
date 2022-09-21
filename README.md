# aap-soeknad-api
___

Applikasjonen er backend for frontend-applikajsonene [ AAP søknader](https://github.com/navikt/aap-soknad) og [Mine arbeidsavklaringspenger](https://github.com/navikt/aap-innsyn).

Den benytter seg av felles forretningslogikk samlet i  [AAP-Domain](https://github.com/navikt/aap-domain)

Applikasjonen har en database for metadata vedrørende:
- metadata knyttet til søknader, eksterne oppgaver og beskjerder på mine aap samt kvitteringer på eksterne notifikasjoner.

# Komme i gang
___

Bygger på JAVA 17 og maven. 
- For å jobbe mot devDB i GCP forutsetter det at du installerer [Colima for mac](https://github.com/abiosoft/colima) `brew install colima` eller benytt docker lokalt.
- Se nødvendig oppsett under [Backend for teamet](https://aap-team-innbygger.intern.nav.no/docs/Komme%20i%20gang/komme-i-gang-med-utvikling)
- å bygge lokalt, krever enten koblig mot GCP-dev db eller å [kommentere ut testene i](src/test/kotlin/no/nav/aap/api/søknad/SøknadDBTest.kt)
- `mvn clean install`


## Henvendelser
___

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

## For NAV-ansatte
___

Interne henvendelser kan sendes via Slack i kanalen #po-aap-innbygger.


