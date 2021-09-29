FROM openjdk:17
COPY target/*.jar "/app/app.jar"
ENV JAVA_OPTS --enable-preview 
