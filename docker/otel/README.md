# OpenTelemetry

`collector-config.yaml` configures the OpenTelemetry collector started by `docker/compose.yaml`.

The app instruments itself with `spring-boot-starter-opentelemetry` and sends traces and metrics to
`OTEL_EXPORTER_OTLP_ENDPOINT`, so it needs no Java agent. Adding the OpenTelemetry Java agent on top
would record every request twice.

If you ever need the agent instead of the starter, remove the starter and download the agent when the
image is built rather than committing the jar:

```dockerfile
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v<version>/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/opentelemetry-javaagent.jar
```
