[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------------< no.nav.aap.api:soknad >------------------------
[INFO] Building aap-soknad-api 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-dependency-plugin:3.1.2:tree (default-cli) @ soknad ---
[INFO] no.nav.aap.api:soknad:jar:0.0.1-SNAPSHOT
[INFO] +- org.springframework.boot:spring-boot-starter-reactor-netty:jar:2.5.6:compile
[INFO] |  \- io.projectreactor.netty:reactor-netty-http:jar:1.0.12:compile
[INFO] |     +- io.netty:netty-codec-http:jar:4.1.69.Final:compile
[INFO] |     |  +- io.netty:netty-common:jar:4.1.69.Final:compile
[INFO] |     |  +- io.netty:netty-buffer:jar:4.1.69.Final:compile
[INFO] |     |  +- io.netty:netty-transport:jar:4.1.69.Final:compile
[INFO] |     |  +- io.netty:netty-codec:jar:4.1.69.Final:compile
[INFO] |     |  \- io.netty:netty-handler:jar:4.1.69.Final:compile
[INFO] |     +- io.netty:netty-codec-http2:jar:4.1.69.Final:compile
[INFO] |     +- io.netty:netty-resolver-dns:jar:4.1.69.Final:compile
[INFO] |     |  +- io.netty:netty-resolver:jar:4.1.69.Final:compile
[INFO] |     |  \- io.netty:netty-codec-dns:jar:4.1.69.Final:compile
[INFO] |     +- io.netty:netty-resolver-dns-native-macos:jar:osx-x86_64:4.1.69.Final:compile
[INFO] |     |  \- io.netty:netty-transport-native-unix-common:jar:4.1.69.Final:compile
[INFO] |     +- io.netty:netty-transport-native-epoll:jar:linux-x86_64:4.1.69.Final:compile
[INFO] |     +- io.projectreactor.netty:reactor-netty-core:jar:1.0.12:compile
[INFO] |     |  \- io.netty:netty-handler-proxy:jar:4.1.69.Final:compile
[INFO] |     \- io.projectreactor:reactor-core:jar:3.4.11:compile
[INFO] |        \- org.reactivestreams:reactive-streams:jar:1.0.3:compile
[INFO] +- org.zalando:problem-spring-web:jar:0.27.0:compile
[INFO] |  +- org.zalando:problem-violations:jar:0.27.0:compile
[INFO] |  +- org.zalando:problem-spring-common:jar:0.27.0:compile
[INFO] |  +- org.apiguardian:apiguardian-api:jar:1.1.2:compile
[INFO] |  +- com.google.code.findbugs:jsr305:jar:3.0.2:compile
[INFO] |  +- org.slf4j:slf4j-api:jar:1.7.32:compile
[INFO] |  +- org.zalando:problem:jar:0.26.0:compile
[INFO] |  +- org.zalando:jackson-datatype-problem:jar:0.26.0:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-databind:jar:2.12.5:compile
[INFO] |  |  \- com.fasterxml.jackson.core:jackson-core:jar:2.12.5:compile
[INFO] |  +- org.zalando:faux-pas:jar:0.9.0:compile
[INFO] |  \- javax.validation:validation-api:jar:2.0.1.Final:compile
[INFO] +- com.neovisionaries:nv-i18n:jar:1.29:compile
[INFO] +- net.logstash.logback:logstash-logback-encoder:jar:6.6:compile
[INFO] +- io.springfox:springfox-boot-starter:jar:3.0.0:compile
[INFO] |  +- io.springfox:springfox-oas:jar:3.0.0:compile
[INFO] |  |  +- io.swagger.core.v3:swagger-annotations:jar:2.1.2:compile
[INFO] |  |  +- io.swagger.core.v3:swagger-models:jar:2.1.2:compile
[INFO] |  |  +- io.springfox:springfox-spi:jar:3.0.0:compile
[INFO] |  |  +- io.springfox:springfox-schema:jar:3.0.0:compile
[INFO] |  |  +- io.springfox:springfox-core:jar:3.0.0:compile
[INFO] |  |  +- io.springfox:springfox-spring-webflux:jar:3.0.0:compile
[INFO] |  |  +- io.springfox:springfox-swagger-common:jar:3.0.0:compile
[INFO] |  |  \- org.mapstruct:mapstruct:jar:1.3.1.Final:runtime
[INFO] |  +- io.springfox:springfox-data-rest:jar:3.0.0:compile
[INFO] |  +- io.springfox:springfox-swagger2:jar:3.0.0:compile
[INFO] |  |  +- io.swagger:swagger-annotations:jar:1.5.20:compile
[INFO] |  |  \- io.swagger:swagger-models:jar:1.5.20:compile
[INFO] |  +- com.fasterxml:classmate:jar:1.5.1:compile
[INFO] |  +- org.springframework.plugin:spring-plugin-core:jar:2.0.0.RELEASE:compile
[INFO] |  \- org.springframework.plugin:spring-plugin-metadata:jar:2.0.0.RELEASE:compile
[INFO] +- io.springfox:springfox-swagger-ui:jar:3.0.0:compile
[INFO] |  \- io.springfox:springfox-spring-webmvc:jar:3.0.0:compile
[INFO] +- io.springfox:springfox-bean-validators:jar:3.0.0:compile
[INFO] |  \- io.springfox:springfox-spring-web:jar:3.0.0:compile
[INFO] |     \- io.github.classgraph:classgraph:jar:4.8.83:compile
[INFO] +- org.springframework.boot:spring-boot-starter-aop:jar:2.5.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter:jar:2.5.6:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:2.5.6:compile
[INFO] |  |  |  +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.14.1:compile
[INFO] |  |  |  |  \- org.apache.logging.log4j:log4j-api:jar:2.14.1:compile
[INFO] |  |  |  \- org.slf4j:jul-to-slf4j:jar:1.7.32:compile
[INFO] |  |  +- jakarta.annotation:jakarta.annotation-api:jar:1.3.5:compile
[INFO] |  |  \- org.yaml:snakeyaml:jar:1.28:compile
[INFO] |  +- org.springframework:spring-aop:jar:5.3.12:compile
[INFO] |  \- org.aspectj:aspectjweaver:jar:1.9.7:compile
[INFO] +- org.projectreactor:reactor-spring:jar:1.0.1.RELEASE:compile
[INFO] |  +- com.jayway.jsonpath:json-path:jar:2.5.0:compile
[INFO] |  |  \- net.minidev:json-smart:jar:2.4.7:compile
[INFO] |  |     \- net.minidev:accessors-smart:jar:2.4.7:compile
[INFO] |  +- org.projectreactor:reactor-core:jar:1.0.1.RELEASE:compile
[INFO] |  |  \- com.lmax:disruptor:jar:3.2.0:compile
[INFO] |  +- org.springframework:spring-beans:jar:5.3.12:compile
[INFO] |  +- org.springframework:spring-context:jar:5.3.12:compile
[INFO] |  +- org.springframework:spring-context-support:jar:5.3.12:compile
[INFO] |  +- org.springframework:spring-core:jar:5.3.12:compile
[INFO] |  |  \- org.springframework:spring-jcl:jar:5.3.12:compile
[INFO] |  +- org.springframework:spring-expression:jar:5.3.12:compile
[INFO] |  \- org.springframework:spring-tx:jar:5.3.12:compile
[INFO] +- com.graphql-java-kickstart:graphql-webclient-spring-boot-starter:jar:1.0.0:compile
[INFO] |  \- com.graphql-java-kickstart:graphql-webclient-spring-boot-autoconfigure:jar:1.0.0:compile
[INFO] |     +- com.graphql-java-kickstart:graphql-webclient:jar:1.0.0:compile
[INFO] |     +- org.springframework.boot:spring-boot-starter-webflux:jar:2.5.6:compile
[INFO] |     |  \- org.springframework:spring-webflux:jar:5.3.12:compile
[INFO] |     \- org.springframework.security:spring-security-oauth2-client:jar:5.5.3:runtime
[INFO] |        +- org.springframework.security:spring-security-core:jar:5.5.3:runtime
[INFO] |        |  \- org.springframework.security:spring-security-crypto:jar:5.5.3:runtime
[INFO] |        +- org.springframework.security:spring-security-oauth2-core:jar:5.5.3:runtime
[INFO] |        \- org.springframework.security:spring-security-web:jar:5.5.3:runtime
[INFO] +- org.hibernate.validator:hibernate-validator:jar:6.2.0.Final:compile
[INFO] |  +- jakarta.validation:jakarta.validation-api:jar:2.0.2:compile
[INFO] |  \- org.jboss.logging:jboss-logging:jar:3.4.2.Final:compile
[INFO] +- org.springframework.boot:spring-boot-starter-jetty:jar:2.5.6:compile
[INFO] |  +- jakarta.servlet:jakarta.servlet-api:jar:4.0.4:compile
[INFO] |  +- jakarta.websocket:jakarta.websocket-api:jar:1.1.2:compile
[INFO] |  +- org.apache.tomcat.embed:tomcat-embed-el:jar:9.0.54:compile
[INFO] |  +- org.eclipse.jetty:jetty-servlets:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty:jetty-continuation:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty:jetty-http:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty:jetty-util:jar:9.4.44.v20210927:compile
[INFO] |  |  \- org.eclipse.jetty:jetty-io:jar:9.4.44.v20210927:compile
[INFO] |  +- org.eclipse.jetty:jetty-webapp:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty:jetty-xml:jar:9.4.44.v20210927:compile
[INFO] |  |  \- org.eclipse.jetty:jetty-servlet:jar:9.4.44.v20210927:compile
[INFO] |  |     +- org.eclipse.jetty:jetty-security:jar:9.4.44.v20210927:compile
[INFO] |  |     |  \- org.eclipse.jetty:jetty-server:jar:9.4.44.v20210927:compile
[INFO] |  |     \- org.eclipse.jetty:jetty-util-ajax:jar:9.4.44.v20210927:compile
[INFO] |  +- org.eclipse.jetty.websocket:websocket-server:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty.websocket:websocket-common:jar:9.4.44.v20210927:compile
[INFO] |  |  |  \- org.eclipse.jetty.websocket:websocket-api:jar:9.4.44.v20210927:compile
[INFO] |  |  +- org.eclipse.jetty.websocket:websocket-client:jar:9.4.44.v20210927:compile
[INFO] |  |  |  \- org.eclipse.jetty:jetty-client:jar:9.4.44.v20210927:compile
[INFO] |  |  \- org.eclipse.jetty.websocket:websocket-servlet:jar:9.4.44.v20210927:compile
[INFO] |  \- org.eclipse.jetty.websocket:javax-websocket-server-impl:jar:9.4.44.v20210927:compile
[INFO] |     +- org.eclipse.jetty:jetty-annotations:jar:9.4.44.v20210927:compile
[INFO] |     |  +- org.eclipse.jetty:jetty-plus:jar:9.4.44.v20210927:compile
[INFO] |     |  +- org.ow2.asm:asm:jar:9.2:compile
[INFO] |     |  \- org.ow2.asm:asm-commons:jar:9.2:compile
[INFO] |     |     +- org.ow2.asm:asm-tree:jar:9.2:compile
[INFO] |     |     \- org.ow2.asm:asm-analysis:jar:9.2:compile
[INFO] |     \- org.eclipse.jetty.websocket:javax-websocket-client-impl:jar:9.4.44.v20210927:compile
[INFO] +- no.nav.security:token-client-spring:jar:1.3.9:compile
[INFO] |  +- no.nav.security:token-validation-core:jar:1.3.9:compile
[INFO] |  +- no.nav.security:token-client-core:jar:1.3.9:compile
[INFO] |  +- org.springframework:spring-web:jar:5.3.12:compile
[INFO] |  +- org.projectlombok:lombok:jar:1.18.22:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.12.5:compile
[INFO] |  +- org.springframework.boot:spring-boot:jar:2.5.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-autoconfigure:jar:2.5.6:compile
[INFO] |  +- com.github.ben-manes.caffeine:caffeine:jar:2.9.2:compile
[INFO] |  |  \- com.google.errorprone:error_prone_annotations:jar:2.5.1:compile
[INFO] |  \- com.nimbusds:nimbus-jose-jwt:jar:9.10.1:compile
[INFO] |     \- com.github.stephenc.jcip:jcip-annotations:jar:1.0-1:compile
[INFO] +- no.nav.security:token-validation-spring:jar:1.3.9:compile
[INFO] |  +- no.nav.security:token-validation-filter:jar:1.3.9:compile
[INFO] |  +- org.springframework:spring-webmvc:jar:5.3.12:compile
[INFO] |  +- com.nimbusds:oauth2-oidc-sdk:jar:9.9.1:compile
[INFO] |  |  +- com.nimbusds:content-type:jar:2.1:compile
[INFO] |  |  \- com.nimbusds:lang-tag:jar:1.5:compile
[INFO] |  \- org.apache.commons:commons-lang3:jar:3.12.0:compile
[INFO] +- no.nav.security:token-validation-spring-test:jar:1.3.9:test
[INFO] |  +- no.nav.security:mock-oauth2-server:jar:0.3.4:test
[INFO] |  |  +- com.squareup.okhttp3:mockwebserver:jar:4.9.1:test
[INFO] |  |  |  +- com.squareup.okhttp3:okhttp:jar:4.9.1:test
[INFO] |  |  |  |  \- com.squareup.okio:okio:jar:2.8.0:test
[INFO] |  |  |  \- junit:junit:jar:4.13.2:test
[INFO] |  |  |     \- org.hamcrest:hamcrest-core:jar:2.2:test
[INFO] |  |  +- ch.qos.logback:logback-classic:jar:1.2.6:compile
[INFO] |  |  +- io.netty:netty-all:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-haproxy:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-memcache:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-mqtt:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-redis:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-smtp:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-socks:jar:4.1.69.Final:compile
[INFO] |  |  |  +- io.netty:netty-codec-stomp:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-codec-xml:jar:4.1.69.Final:test
[INFO] |  |  |  +- com.fasterxml:aalto-xml:jar:1.0.0:test
[INFO] |  |  |  +- org.codehaus.woodstox:stax2-api:jar:4.0.0:test
[INFO] |  |  |  +- io.netty:netty-transport-rxtx:jar:4.1.69.Final:test
[INFO] |  |  |  +- org.rxtx:rxtx:jar:2.1.7:test
[INFO] |  |  |  +- io.netty:netty-transport-sctp:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-transport-udt:jar:4.1.69.Final:test
[INFO] |  |  |  +- com.barchart.udt:barchart-udt-bundle:jar:2.3.0:test
[INFO] |  |  |  +- io.netty:netty-transport-native-epoll:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-transport-native-kqueue:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-resolver-dns-native-macos:jar:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-transport-native-epoll:jar:linux-aarch_64:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-transport-native-kqueue:jar:osx-x86_64:4.1.69.Final:test
[INFO] |  |  |  +- io.netty:netty-transport-native-kqueue:jar:osx-aarch_64:4.1.69.Final:test
[INFO] |  |  |  \- io.netty:netty-resolver-dns-native-macos:jar:osx-aarch_64:4.1.69.Final:test
[INFO] |  |  +- io.github.microutils:kotlin-logging-jvm:jar:2.0.6:test
[INFO] |  |  +- org.freemarker:freemarker:jar:2.3.31:test
[INFO] |  |  \- org.bouncycastle:bcpkix-jdk15on:jar:1.68:test
[INFO] |  |     \- org.bouncycastle:bcprov-jdk15on:jar:1.68:test
[INFO] |  \- org.springframework.boot:spring-boot-test-autoconfigure:jar:2.5.6:test
[INFO] +- no.nav.security:token-validation-test-support:jar:1.3.9:test
[INFO] |  +- org.springframework.boot:spring-boot-starter-jersey:jar:2.5.6:test
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-validation:jar:2.5.6:test
[INFO] |  |  +- org.glassfish.jersey.core:jersey-server:jar:2.33:test
[INFO] |  |  |  +- org.glassfish.jersey.core:jersey-common:jar:2.33:test
[INFO] |  |  |  |  \- org.glassfish.hk2:osgi-resource-locator:jar:1.0.3:test
[INFO] |  |  |  +- org.glassfish.jersey.core:jersey-client:jar:2.33:test
[INFO] |  |  |  +- jakarta.ws.rs:jakarta.ws.rs-api:jar:2.1.6:test
[INFO] |  |  |  \- org.glassfish.hk2.external:jakarta.inject:jar:2.6.1:test
[INFO] |  |  +- org.glassfish.jersey.containers:jersey-container-servlet-core:jar:2.33:test
[INFO] |  |  +- org.glassfish.jersey.containers:jersey-container-servlet:jar:2.33:test
[INFO] |  |  +- org.glassfish.jersey.ext:jersey-bean-validation:jar:2.33:test
[INFO] |  |  +- org.glassfish.jersey.ext:jersey-spring5:jar:2.33:test
[INFO] |  |  |  +- org.glassfish.jersey.inject:jersey-hk2:jar:2.33:test
[INFO] |  |  |  |  +- org.glassfish.hk2:hk2-locator:jar:2.6.1:test
[INFO] |  |  |  |  |  \- org.glassfish.hk2.external:aopalliance-repackaged:jar:2.6.1:test
[INFO] |  |  |  |  \- org.javassist:javassist:jar:3.25.0-GA:test
[INFO] |  |  |  +- org.glassfish.hk2:hk2:jar:2.6.1:test
[INFO] |  |  |  |  +- org.glassfish.hk2:hk2-utils:jar:2.6.1:test
[INFO] |  |  |  |  +- org.glassfish.hk2:hk2-api:jar:2.6.1:test
[INFO] |  |  |  |  +- org.glassfish.hk2:hk2-core:jar:2.6.1:test
[INFO] |  |  |  |  +- org.glassfish.hk2:hk2-runlevel:jar:2.6.1:test
[INFO] |  |  |  |  \- org.glassfish.hk2:class-model:jar:2.6.1:test
[INFO] |  |  |  |     \- org.ow2.asm:asm-util:jar:7.1:test
[INFO] |  |  |  \- org.glassfish.hk2:spring-bridge:jar:2.6.1:test
[INFO] |  |  \- org.glassfish.jersey.media:jersey-media-json-jackson:jar:2.33:test
[INFO] |  |     +- org.glassfish.jersey.ext:jersey-entity-filtering:jar:2.33:test
[INFO] |  |     \- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:jar:2.12.5:test
[INFO] |  \- javax.inject:javax.inject:jar:1:test
[INFO] +- org.springframework.boot:spring-boot-starter-actuator:jar:2.5.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-actuator-autoconfigure:jar:2.5.6:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-actuator:jar:2.5.6:compile
[INFO] |  |  \- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.12.5:compile
[INFO] |  \- io.micrometer:micrometer-core:jar:1.7.5:compile
[INFO] |     +- org.hdrhistogram:HdrHistogram:jar:2.1.12:compile
[INFO] |     \- org.latencyutils:LatencyUtils:jar:2.0.3:runtime
[INFO] +- org.springframework.boot:spring-boot-starter-data-jdbc:jar:2.5.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter-jdbc:jar:2.5.6:compile
[INFO] |  |  +- com.zaxxer:HikariCP:jar:4.0.3:compile
[INFO] |  |  \- org.springframework:spring-jdbc:jar:5.3.12:compile
[INFO] |  \- org.springframework.data:spring-data-jdbc:jar:2.2.6:compile
[INFO] |     +- org.springframework.data:spring-data-relational:jar:2.2.6:compile
[INFO] |     \- org.springframework.data:spring-data-commons:jar:2.5.6:compile
[INFO] +- org.springframework.boot:spring-boot-starter-web:jar:2.5.6:compile
[INFO] |  \- org.springframework.boot:spring-boot-starter-json:jar:2.5.6:compile
[INFO] |     +- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:jar:2.12.5:compile
[INFO] |     \- com.fasterxml.jackson.module:jackson-module-parameter-names:jar:2.12.5:compile
[INFO] +- org.springframework.kafka:spring-kafka:jar:2.7.8:compile
[INFO] |  +- org.springframework:spring-messaging:jar:5.3.12:compile
[INFO] |  +- org.springframework.retry:spring-retry:jar:1.3.1:compile
[INFO] |  |  \- javax.annotation:javax.annotation-api:jar:1.3.2:compile
[INFO] |  \- org.apache.kafka:kafka-clients:jar:2.7.1:compile
[INFO] |     +- com.github.luben:zstd-jni:jar:1.4.5-6:compile
[INFO] |     +- org.lz4:lz4-java:jar:1.7.1:compile
[INFO] |     \- org.xerial.snappy:snappy-java:jar:1.1.7.7:compile
[INFO] +- org.springframework.boot:spring-boot-devtools:jar:2.5.6:runtime (optional) 
[INFO] +- io.micrometer:micrometer-registry-prometheus:jar:1.7.5:runtime
[INFO] |  \- io.prometheus:simpleclient_common:jar:0.10.0:runtime
[INFO] |     \- io.prometheus:simpleclient:jar:0.10.0:runtime
[INFO] +- org.postgresql:postgresql:jar:42.2.24:runtime
[INFO] |  \- org.checkerframework:checker-qual:jar:3.5.0:compile
[INFO] +- org.springframework.boot:spring-boot-configuration-processor:jar:2.5.6:compile (optional) 
[INFO] +- org.springframework.boot:spring-boot-starter-test:jar:2.5.6:test
[INFO] |  +- org.springframework.boot:spring-boot-test:jar:2.5.6:test
[INFO] |  +- jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3:test
[INFO] |  |  \- jakarta.activation:jakarta.activation-api:jar:1.2.2:test
[INFO] |  +- org.assertj:assertj-core:jar:3.19.0:test
[INFO] |  +- org.hamcrest:hamcrest:jar:2.2:test
[INFO] |  +- org.junit.jupiter:junit-jupiter:jar:5.7.2:test
[INFO] |  |  +- org.junit.jupiter:junit-jupiter-params:jar:5.7.2:test
[INFO] |  |  \- org.junit.jupiter:junit-jupiter-engine:jar:5.7.2:test
[INFO] |  |     \- org.junit.platform:junit-platform-engine:jar:1.7.2:test
[INFO] |  +- org.mockito:mockito-core:jar:3.9.0:test
[INFO] |  |  +- net.bytebuddy:byte-buddy:jar:1.10.22:compile
[INFO] |  |  +- net.bytebuddy:byte-buddy-agent:jar:1.10.22:test
[INFO] |  |  \- org.objenesis:objenesis:jar:3.2:test
[INFO] |  +- org.mockito:mockito-junit-jupiter:jar:3.9.0:test
[INFO] |  +- org.skyscreamer:jsonassert:jar:1.5.0:test
[INFO] |  |  \- com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test
[INFO] |  +- org.springframework:spring-test:jar:5.3.12:test
[INFO] |  \- org.xmlunit:xmlunit-core:jar:2.8.3:test
[INFO] +- org.springframework.kafka:spring-kafka-test:jar:2.7.8:test
[INFO] |  +- org.apache.kafka:kafka-clients:jar:test:2.7.1:test
[INFO] |  +- org.apache.kafka:kafka-streams:jar:2.7.1:test
[INFO] |  |  +- org.apache.kafka:connect-json:jar:2.7.1:test
[INFO] |  |  |  \- org.apache.kafka:connect-api:jar:2.7.1:test
[INFO] |  |  \- org.rocksdb:rocksdbjni:jar:5.18.4:test
[INFO] |  +- org.apache.kafka:kafka-streams-test-utils:jar:2.7.1:test
[INFO] |  +- org.apache.kafka:kafka_2.13:jar:2.7.1:test
[INFO] |  |  +- org.apache.kafka:kafka-raft:jar:2.7.1:test
[INFO] |  |  +- com.fasterxml.jackson.module:jackson-module-scala_2.13:jar:2.12.5:test
[INFO] |  |  |  \- com.thoughtworks.paranamer:paranamer:jar:2.8:test
[INFO] |  |  +- com.fasterxml.jackson.dataformat:jackson-dataformat-csv:jar:2.12.5:test
[INFO] |  |  +- net.sf.jopt-simple:jopt-simple:jar:5.0.4:test
[INFO] |  |  +- com.yammer.metrics:metrics-core:jar:2.2.0:test
[INFO] |  |  +- org.scala-lang.modules:scala-collection-compat_2.13:jar:2.2.0:test
[INFO] |  |  +- org.scala-lang.modules:scala-java8-compat_2.13:jar:0.9.1:test
[INFO] |  |  +- org.scala-lang:scala-library:jar:2.13.3:test
[INFO] |  |  +- org.scala-lang:scala-reflect:jar:2.13.3:test
[INFO] |  |  +- com.typesafe.scala-logging:scala-logging_2.13:jar:3.9.2:test
[INFO] |  |  +- org.apache.zookeeper:zookeeper:jar:3.5.9:test
[INFO] |  |  |  +- org.apache.zookeeper:zookeeper-jute:jar:3.5.9:test
[INFO] |  |  |  \- org.apache.yetus:audience-annotations:jar:0.5.0:test
[INFO] |  |  \- commons-cli:commons-cli:jar:1.4:test
[INFO] |  +- org.apache.kafka:kafka_2.13:jar:test:2.7.1:test
[INFO] |  \- org.junit.jupiter:junit-jupiter-api:jar:5.7.2:test
[INFO] |     +- org.opentest4j:opentest4j:jar:1.2.0:test
[INFO] |     \- org.junit.platform:junit-platform-commons:jar:1.7.2:test
[INFO] +- com.h2database:h2:jar:1.4.200:runtime
[INFO] +- no.nav.boot:boot-conditionals:jar:2.0.13:compile
[INFO] |  \- ch.qos.logback:logback-core:jar:1.2.6:compile
[INFO] +- com.fasterxml.jackson.module:jackson-module-kotlin:jar:2.12.5:compile
[INFO] +- org.jetbrains.kotlin:kotlin-stdlib-jdk8:jar:1.5.31:compile
[INFO] |  +- org.jetbrains.kotlin:kotlin-stdlib:jar:1.5.31:compile
[INFO] |  |  +- org.jetbrains:annotations:jar:13.0:compile
[INFO] |  |  \- org.jetbrains.kotlin:kotlin-stdlib-common:jar:1.5.31:compile
[INFO] |  \- org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar:1.5.31:compile
[INFO] +- org.jetbrains.kotlin:kotlin-reflect:jar:1.5.31:compile
[INFO] \- org.jetbrains.kotlin:kotlin-test:jar:1.5.31:test
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.444 s
[INFO] Finished at: 2021-11-03T13:12:50+01:00
[INFO] ------------------------------------------------------------------------
