package no.nav.aap.api.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AliasFor
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Bean
@Documented
@Qualifier
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
annotation class QualifiedBean(
    @get:AliasFor(annotation = Qualifier::class, attribute = "value")
    val value: String)