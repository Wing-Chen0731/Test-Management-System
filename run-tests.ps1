docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker Desktop is not running."
    exit 1
}

Write-Host "Running MySQL and Redis Testcontainers integration test..."
mvn -q test-compile "-Dit.test=MultiContainerDemoIT" failsafe:integration-test failsafe:verify
exit $LASTEXITCODE
