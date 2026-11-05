FROM eclipse-temurin:25-jdk-alpine@sha256:425f0b733cb23843bd78dbe5552513976d0cf12b9722752c48a5161cf5cd2f32 AS builder

WORKDIR /build

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre-alpine@sha256:56f273059de6b13a6b49fe0ded691ed2d0399bbee76156a8f2d385f7524a3d43

# The pinned base lags behind Alpine security fixes until Temurin publishes a new digest
RUN apk upgrade --no-cache \
    && addgroup -S pantrio \
    && adduser -S -D -H -G pantrio pantrio \
    && mkdir -p /opt/logs \
    && chown pantrio:pantrio /opt/logs

ENV SERVER_PORT=8080
ENV TZ=Europe/Zagreb
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

WORKDIR /app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

USER pantrio

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
