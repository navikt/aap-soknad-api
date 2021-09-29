FROM openjdk:17
ENV APP_NAME=aap-hello-world
COPY target/*.jar "/app/app.jar"
ENV JAVA_OPTS --enable-preview 
