FROM eclipse-temurin:25-jdk-alpine@sha256:c7c3d0bdf7f2bbc5a05a6712ed334d198360105634ea9adf76fca94f381b1e3f AS builder

WORKDIR /build

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre-alpine@sha256:e1898fd6a11fc965a994d8ace186fd3585aeb636cf8f05f47a723fcfee0b0918

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
