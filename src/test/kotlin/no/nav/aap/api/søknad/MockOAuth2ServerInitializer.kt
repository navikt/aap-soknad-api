package no.nav.aap.api.søknad
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class MockOAuth2ServerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(ctx: ConfigurableApplicationContext) {
      //  val server = registerMockOAuth2Server(ctx as GenericApplicationContext)
        //val baseUrl = server.baseUrl().toString().replace("/$".toRegex(), "")
       // TestPropertyValues.of(MOCK_OAUTH_2_SERVER_BASE_URL, baseUrl).applyTo(ctx)
    }

    /*private fun registerMockOAuth2Server(ctx: GenericApplicationContext): MockOAuth2Server =
        MockOAuth2Server(route("/pdl/graphql") { OAuth2HttpResponse(status = 200, body = "pdl") }
        ).apply {
            start()
            ctx.registerBean(MockOAuth2Server::class.java, Supplier{ this })
        }
        */


    companion object {
        private const val MOCK_OAUTH_2_SERVER_BASE_URL = "mock-oauth2-server.baseUrl"
    }
}