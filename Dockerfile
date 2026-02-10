FROM bellsoft/liberica-openjdk-alpine:25 AS builder
WORKDIR /build

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjre-alpine:25
WORKDIR /app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

COPY docker/otel/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/opentelemetry-javaagent.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
