# ---- Build ----
# Builda o fast-jar dentro do container, para que `docker compose up --build` faça tudo do
# zero, sem exigir um `./mvnw package` local antes.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Sem testes: eles sobem um MySQL via Dev Services (precisa de Docker), indisponível no build.
RUN mvn -B clean package -DskipTests

# ---- Runtime ----
# Espelha o Dockerfile.jvm do scaffold (mesma base, run-java.sh e ajuste de host 0.0.0.0).
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.24

ENV LANGUAGE='en_US:en'

# A base ubi9-minimal já inclui o curl (curl-minimal), usado pelo healthcheck do compose.

COPY --from=build --chown=185 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /build/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
