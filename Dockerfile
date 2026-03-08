FROM bellsoft/liberica-openjdk-alpine:21 AS builder
WORKDIR /build

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM bellsoft/liberica-openjre-alpine:21
WORKDIR /app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
