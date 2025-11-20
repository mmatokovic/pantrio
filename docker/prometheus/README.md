
1. make sure http://localhost:8081/actuator/prometheus returns data:
   `management.endpoints.web.exposure.include=prometheus,health`

   - add to pom.xml:
     - spring-boot-starter-actuator
     - io.micronaut.micrometer:micronaut-micrometer-registry-prometheus:3.3.0

2. Install prometheus add this to prometheus.yml as a child of `scrape_configs`

    ```
    - job_name: 'spring'
      metrics_path: '/actuator/prometheus'
      static_configs:
      - targets: ['localhost:8081']
    ```

3. Grafana http://localhost:3000/ login with `admin/admin` Add a datasource pointing to:

    ```plain
    http://prometheus:9090
    ```

4. Create a Panel and Add Queries

- Maximum HTTP request time:

    ```plain
    http_server_requests_seconds_max
    ```

- Requests per minute:

    ```plain
    rate(http_server_requests_seconds_count[1m])
    ```

- Cache size (after adding cache management):

    ```plain
    cache_size
    ```
