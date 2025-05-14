$pat = "ghp_4s9LclaMLg3QYgoIfer2yPKHGsv9VZ32h04"
$headers = @{
    Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(":$pat"))
}

Invoke-WebRequest `
  -Uri "https://hrgtec.pkgs.visualstudio.com/_packaging/hrgtec/nuget/v3/index.json" `
  -Headers $headers `
  -UseBasicParsing
