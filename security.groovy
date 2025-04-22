- name: Find all Java installations
        shell: powershell
        run: |
          Write-Host "📍 Searching for installed Java versions..."
          Get-ChildItem -Recurse -Path 'C:\', 'D:\' -Include java.exe -ErrorAction SilentlyContinue -Force |
            Where-Object { $_.FullName -match '\\bin\\java\.exe$' } |
            ForEach-Object {
              Write-Host "🔹 Found: $($_.FullName)"
              & "$($_.FullName)" -version
            }
