package no.nav.aap.api.joark

import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class JoarkConverterTest {

    @Mock
    lateinit var lager: Dokumentlager
}