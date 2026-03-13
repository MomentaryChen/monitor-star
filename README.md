# Spring Boot Monitoring Stack

Monitors Spring Boot applications using **Prometheus**, **Grafana**, **Loki**, and **Promtail**, all running in Docker.

The stack includes:

- **Prometheus**: scrapes metrics from multiple Spring Boot services (`springboot-app`, `order-service`, ...).
- **Grafana**: pre-built dashboards for Spring Boot metrics, Spring Boot logs, and Docker logs.
- **Loki + Promtail**: collects and indexes application logs and container logs for query in Grafana.

> 🌐 For Chinese version, see [`README.zh-TW.md`](README.zh-TW.md).

## Prerequisites

Your Spring Boot app must expose Prometheus metrics. Add to `pom.xml`:

```xml
<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus Registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

And in `application.properties` / `application.yml`:

```properties
# Expose actuator endpoints
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

## Directory Structure

```
monitor-star/
├── docker-compose.yml
├── prometheus/
│   └── prometheus.yml          # Prometheus scrape config
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/
│   │   │   └── prometheus.yml  # Auto-configure Prometheus datasource
│   │   └── dashboards/
│   │       └── dashboards.yml  # Dashboard provider config
│   └── dashboards/
│       └── springboot.json     # Pre-built Spring Boot dashboard
└── app/                        # (optional) your Spring Boot source
    └── Dockerfile
```

## Usage

### 1. Build & start (Docker only)

```bash
# If using a pre-built image, comment out the `build:` block in docker-compose.yml
# and set image: your-registry/your-app:tag

docker-compose up -d
```

### 1b. Build with helper script (`build.ps1`)

On Windows / PowerShell you can use the provided build script:

```powershell
# Default: Maven build (skip tests) + docker compose build
.\build.ps1

# Run tests during Maven build
.\build.ps1 -SkipTests:$false

# Only build JARs, skip docker compose
.\build.ps1 -NoDockerCompose

# Build and start docker compose with multiple replicas
.\build.ps1 -Up -Replicas 3 -OrderReplicas 2

# Show README locations (no build)
.\build.ps1 -Readme
```

### 2. Access the services

| Service    | URL                        | Credentials       |
|------------|----------------------------|-------------------|
| App        | http://localhost:8080      | —                 |
| Prometheus | http://localhost:9090      | —                 |
| Grafana    | http://localhost:3000      | admin / admin123  |

### 3. View dashboards

Grafana auto-provisions the **"Spring Boot Monitoring"** dashboard under the
**Spring Boot** folder. It includes:

- ✅ App health / uptime
- 📈 HTTP request rate & response times (p99)
- 🧠 JVM heap & non-heap memory
- 🧵 JVM thread counts
- ⚙️ CPU usage

### 4. Stop

```bash
docker-compose down           # stop containers (keep volumes)
docker-compose down -v        # stop + delete volumes (reset data)
```

## Customization

- **Change Grafana password**: set `GF_SECURITY_ADMIN_PASSWORD` in `docker-compose.yml`
- **Add more apps**: add a new `job_name` block in `prometheus/prometheus.yml`
- **Import community dashboards**: download JSON from https://grafana.com/grafana/dashboards
  and place it in `grafana/dashboards/`; popular IDs for Spring Boot: **4701**, **12900**

## License

This project is open-sourced under the **MIT License**.  
See the [`LICENSE`](LICENSE) file for details.

## Screenshots

### Spring Boot metrics dashboard

![Spring Boot metrics dashboard](img/springboot-monitoring/dashboard.png)

### Spring Boot logs dashboard

The **Spring Boot — Logs** dashboard (under the `Spring Boot` folder) lets you:

- **Service-level filtering**: filter logs by Spring Boot service and instance IP.
- **Error / warn overview**: see total log volume and counts by level (ERROR/WARN/INFO/DEBUG).
- **Top noisy modules**: quickly find top error loggers by class name.
- **Trace / keyword search**: search by logger, traceId, or any keyword across all selected services.

![Spring Boot logs dashboard](img/springboot-log/dashboard.png)

### Log explorer (Loki / logs view)

![Spring Boot log explorer](img/springboot-log/log-explorer.png)

### Docker logs dashboard

The **Docker — Logs** dashboard (under the `Docker` folder) gives you:

- **Container-level filtering**: focus on specific Docker containers running on the host.
- **Error / warn overview**: see log volume and level distribution per container.
- **Hot containers**: quickly spot containers generating the most logs or errors.

![Docker logs dashboard](img/docker-log/dashboard.png)

## Author

Maintainer: **MomentaryChen** (`zzser15963@gmail.com`)
