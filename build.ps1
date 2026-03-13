Param(
    [switch]$SkipTests = $true,
    [switch]$NoDockerCompose = $false,
    [switch]$Up = $false,
    [int]$Replicas = 3,
    [int]$OrderReplicas = 2
)

Write-Host "=== Step 1: Maven build springboot-app ===" -ForegroundColor Cyan

$mvnCmd = "mvn"
if ($SkipTests) {
    $mvnArgs = "clean package -DskipTests"
} else {
    $mvnArgs = "clean package"
}

Push-Location "springboot-app"
Write-Host "Running: mvn $mvnArgs" -ForegroundColor Yellow

$mvn = Start-Process -FilePath $mvnCmd -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru
if ($mvn.ExitCode -ne 0) {
    Write-Host "Maven build failed with exit code $($mvn.ExitCode)." -ForegroundColor Red
    Pop-Location
    exit $mvn.ExitCode
}

Pop-Location
Write-Host "Maven build success." -ForegroundColor Green

Write-Host "=== Step 1b: Maven build order-service ===" -ForegroundColor Cyan
Push-Location "order-service"
Write-Host "Running: mvn $mvnArgs" -ForegroundColor Yellow

$mvn2 = Start-Process -FilePath $mvnCmd -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru
if ($mvn2.ExitCode -ne 0) {
    Write-Host "Maven build failed with exit code $($mvn2.ExitCode)." -ForegroundColor Red
    Pop-Location
    exit $mvn2.ExitCode
}

Pop-Location
Write-Host "Maven build success." -ForegroundColor Green

if ($NoDockerCompose) {
    Write-Host "Skip docker-compose build/up as requested." -ForegroundColor Yellow
    exit 0
}

Write-Host "=== Step 2: docker compose build (springboot-app image) ===" -ForegroundColor Cyan

Write-Host "Running: docker compose build" -ForegroundColor Yellow
& docker compose build
if ($LASTEXITCODE -ne 0) {
    Write-Host "docker compose build failed with exit code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "docker compose build success." -ForegroundColor Green

if ($Up) {
    Write-Host "=== Step 3: docker compose up -d --scale springboot-app=$Replicas --scale order-service=$OrderReplicas ===" -ForegroundColor Cyan
    Write-Host "Running: docker compose up -d --scale springboot-app=$Replicas --scale order-service=$OrderReplicas" -ForegroundColor Yellow

    # Docker Compose v2: 使用 `docker compose` 而非 `docker-compose`
    & docker compose up -d --scale springboot-app=$Replicas --scale order-service=$OrderReplicas
    if ($LASTEXITCODE -ne 0) {
        Write-Host "docker-compose up failed with exit code $LASTEXITCODE." -ForegroundColor Red
        exit $LASTEXITCODE
    }

    Write-Host "docker-compose up -d success." -ForegroundColor Green
}

Write-Host "Build script completed." -ForegroundColor Cyan

