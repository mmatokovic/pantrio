FROM registry.access.redhat.com/ubi8/openjdk-21:latest AS builder

WORKDIR /deployments

RUN chgrp -R 0 /deployments && \
    chmod -R g=u /deployments

COPY target/*.jar app.jar
RUN jar -xf app.jar

FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:latest

WORKDIR /home/jboss

COPY --from=builder /deployments/app.jar .

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "dev.mmatokovic.pantrio.PantrioApplication"]