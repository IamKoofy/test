$taskIdPattern = 'https:\/\/codequality\.gbt\.gbtad\.com\/api\/ce\/task\?id=([a-zA-Z0-9\-_]+)'
$ceTaskId = $null

foreach ($line in $sonarOutput) {
  if ($line -match $taskIdPattern) {
    $ceTaskId = $Matches[1]
    Write-Host "Matched line: $line"
    break
  }
}

if ($null -ne $ceTaskId) {
  Write-Host "Found ceTaskId: $ceTaskId"
  "ceTaskId=$ceTaskId" >> $env:GITHUB_OUTPUT
} else {
  Write-Error "❌ Could not extract ceTaskId from scanner output."
  exit 1
}
