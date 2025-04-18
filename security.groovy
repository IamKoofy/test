param (
    [string]$ProjectName,
    [string]$SonarHostUrl,
    [string]$SonarToken
)

Write-Host "🔚 Running dotnet sonarscanner end..."
dotnet sonarscanner end /d:sonar.login=$SonarToken

$reportPath = ".sonarqube\out\report-task.txt"
if (-not (Test-Path $reportPath)) {
    Write-Error "report-task.txt not found at $reportPath"
    exit 1
}

$lines = Get-Content $reportPath
$ceTaskUrl = ($lines | Where-Object { $_ -like "ceTaskUrl=*" }) -replace "ceTaskUrl=", ""

if (-not $ceTaskUrl) {
    Write-Error "ceTaskUrl not found in report-task.txt"
    exit 1
}

$authHeader = @{
    Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${SonarToken}:"))
}

$maxRetries = 60
$delay = 5
$status = ""
$retry = 0

do {
    Start-Sleep -Seconds $delay
    try {
        $res = Invoke-RestMethod -Uri $ceTaskUrl -Headers $authHeader -UseBasicParsing
        $status = $res.task.status
        Write-Host "SonarQube background task status: $status"
    } catch {
        Write-Warning "Retrying due to error fetching task status..."
    }
    $retry++
} while ($status -ne "SUCCESS" -and $retry -lt $maxRetries)

if ($status -ne "SUCCESS") {
    Write-Error "Analysis did not complete in time."
    exit 1
}

$gateCheckUrl = "$SonarHostUrl/api/qualitygates/project_status?projectKey=$ProjectName"
$gate = Invoke-RestMethod -Uri $gateCheckUrl -Headers $authHeader -UseBasicParsing
$gateStatus = $gate.projectStatus.status

Write-Host "✅ Quality Gate Status: $gateStatus"

if ($gateStatus -ne "OK") {
    Write-Error "❌ Quality Gate failed: $gateStatus"
    exit 1
}
