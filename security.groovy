param (
    [string]$ProjectName,
    [string]$SonarHostUrl,
    [string]$SonarToken,
    [string]$AdditionalArgs
)

Write-Host "📡 Running sonar-scanner..."
$scannerCommand = @(
    "sonar-scanner",
    "-Dsonar.projectKey=$ProjectName",
    "-Dsonar.host.url=$SonarHostUrl",
    "-Dsonar.login=$SonarToken"
)

if ($AdditionalArgs) {
    $scannerCommand += $AdditionalArgs
}

# Run the scanner
& $scannerCommand

$reportPath = ".scannerwork\report-task.txt"
if (-not (Test-Path $reportPath)) {
    Write-Error "❌ report-task.txt not found at $reportPath"
    exit 1
}

$taskInfo = Get-Content $reportPath | ConvertFrom-StringData
$ceTaskId = $taskInfo["ceTaskId"]

if (-not $ceTaskId) {
    Write-Error "❌ ceTaskId not found in report-task.txt"
    exit 1
}

$authBytes = [Text.Encoding]::ASCII.GetBytes("$SonarToken:")
$authHeader = @{
    Authorization = "Basic " + [Convert]::ToBase64String($authBytes)
}

# Poll the task until it finishes
$maxRetries = 30
$delay = 5
$retry = 0
$status = ""

do {
    Start-Sleep -Seconds $delay
    try {
        $response = Invoke-RestMethod -Uri "$SonarHostUrl/api/ce/task?id=$ceTaskId" -Headers $authHeader -UseBasicParsing
        $status = $response.task.status
        Write-Host "🔄 SonarQube analysis status: $status"
    } catch {
        Write-Warning "⚠️ Failed to get analysis status. Retrying..."
    }
    $retry++
} while ($status -ne "SUCCESS" -and $retry -lt $maxRetries)

if ($status -ne "SUCCESS") {
    Write-Error "❌ Analysis did not complete in expected time."
    exit 1
}

# Check quality gate status
$gateResponse = Invoke-RestMethod -Uri "$SonarHostUrl/api/qualitygates/project_status?projectKey=$ProjectName" -Headers $authHeader -UseBasicParsing
$gateStatus = $gateResponse.projectStatus.status

Write-Host "✅ Quality Gate status: $gateStatus"

if ($gateStatus -ne "OK") {
    Write-Error "❌ Quality Gate failed with status: $gateStatus"
    exit 1
}
