FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre-alpine

ENV SERVER_PORT=8080

WORKDIR /app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
