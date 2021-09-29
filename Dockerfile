FROM navikt/java:17-appdynamics
COPY target/*.jar "/app/app.jar"
ENV JAVA_OPTS --enable-preview 
