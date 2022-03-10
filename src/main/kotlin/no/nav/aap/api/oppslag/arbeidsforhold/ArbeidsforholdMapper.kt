package no.nav.aap.api.oppslag.arbeidsforhold

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArbeidsforholdMapper {
    companion object {
        private const val PERIODE = "periode"
        private const val ARBEIDSAVTALER = "arbeidsavtaler"
        private const  val TYPE = "type"
        private const  val OFFENTLIG_IDENT = "offentligIdent"
        private const  val PERSON = "Person"
        private const  val ORGNR = "orgnr"
        private const  val ORGANISASJON = "Organisasjon"
        private const  val STILLINGSPROSENT = "stillingsprosent"
        private const  val GYLDIGHETSPERIODE = "gyldighetsperiode"
        private const  val TOM = "tom"
        private const  val ORGANISASJONSNUMMER = "organisasjonsnummer"
        private const  val FOM = "fom"
        private const  val ARBEIDSGIVER = "arbeidsgiver"
        private const  val ANSETTELSESPERIODE = "ansettelsesperiode"
        private  val LOG = LoggerFactory.getLogger(ArbeidsforholdMapper::class.java)

    }
    /*
    private fun pro(avtaler: List<*>) {
        return avtaler.map { it as Map<*,*> }
            .map { MapUtil::getAs(it, STILLINGSPROSENT, Double.class))}
            .map(ProsentAndel::new)
    }

     */

    //private final OrganisasjonConnection organisasjon
    /*



    ArbeidsforholdMapper(OrganisasjonConnection organisasjon) {
        this.organisasjon = organisasjon;
    }

    EnkeltArbeidsforhold tilArbeidsforhold(Map<?, ?> map) {
        var id = id(get(map, ARBEIDSGIVER, Map.class));
        var periode = get(get(map, ANSETTELSESPERIODE, Map.class), PERIODE, Map.class);
        return EnkeltArbeidsforhold.builder()
                .arbeidsgiverId(id.getFirst())
                .arbeidsgiverIdType(id.getSecond())
                .from(dato(get(periode, FOM)))
                .to(Optional.ofNullable(dato(get(periode, TOM))))
                .stillingsprosent(stillingsprosent(get(map, ARBEIDSAVTALER, List.class)))
                .arbeidsgiverNavn(organisasjon.navn(id.getFirst()))
                .build();
    }

    private static ProsentAndel stillingsprosent(List<?> avtaler) {
        return avtaler.stream()
                .map(Map.class::cast)
                .filter(ArbeidsforholdMapper::erGjeldende)
                .map(a -> get(a, STILLINGSPROSENT, Double.class))
                .filter(Objects::nonNull)
                .map(ProsentAndel::new)
                .findFirst()
                .orElse(null);
    }

    private static boolean erGjeldende(Map<?, ?> avtale) {
        var periode = get(avtale, GYLDIGHETSPERIODE, Map.class);
        var fom = dato(get(periode, FOM));
        if (fom.isAfter(LocalDate.now())) {
            LOG.trace("Fremtidig arbeidsforhold begynner {}", fom);
            return true;
        }
        var tom = Optional.ofNullable(dato(get(periode, TOM)))
                .orElse(LocalDate.now());
        var gyldig = nowWithinPeriod(fom, tom);
        LOG.trace("Arbeidsorhold gyldig: {}", gyldig);
        return gyldig;
    }

    private static Pair<String, String> id(Map<?, ?> map) {
        return switch (get(map, TYPE)) {
            case ORGANISASJON -> Pair.of(get(map, ORGANISASJONSNUMMER), ORGNR);
            case PERSON -> Pair.of(get(map, OFFENTLIG_IDENT), FNR);
            default -> throw new IllegalArgumentException("Fant verken orgnr eller fnr i " + map);
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[organisasjon=" + organisasjon + "]";
    }

}
     */
}