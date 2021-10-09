package no.nav.aap.api.søknad.domain;

import com.neovisionaries.i18n.CountryCode;

public record UtenlandsSøknad(CountryCode land, Periode periode) {
}
