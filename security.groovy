name: Build and Test on PR

on:
  pull_request:
    branches:
      - main  # or whatever your default branch is

jobs:
  build:
    name: Build and Analyze
    runs-on: windows-latest
    defaults:
      run:
        shell: powershell

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup .NET SDK
        uses: your-org/your-shared-repo/.github/actions/setup-dotnet@main
        with:
          dotnet-version: '8.0.100' # Replace with actual version you use

      - name: Build solution
        run: dotnet build ${{ github.event.pull_request.head.repo.full_name }} --configuration Release

      - name: Snyk scan
        if: env.SNYK_TOKEN != ''
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        run: |
          dotnet tool install --global Snyk.CLI
          snyk test --org=${{ secrets.SNYK_ORG }} --project-name=${{ github.repository }}

      - name: Run Tests with Code Coverage
        run: |
          dotnet test 'path\to\test\project.csproj' --configuration Release `
            --filter "(TestCategory=Unit)|(TestCategory=Component)" `
            --collect "Code Coverage"

      - name: Publish project
        run: |
          dotnet publish 'path\to\main\project.csproj' --no-build --no-restore `
            -o d:\DockerShare\${{ github.run_id }}\output -c Release

      - name: Docker Build
        run: |
          docker build -f Dockerfile `
            -t yourrepo/image:${{ github.run_number }} `
            d:\DockerShare\${{ github.run_id }}\output

      - name: Docker Push
        env:
          DOCKER_USER: ${{ secrets.DOCKER_USER }}
          DOCKER_PASS: ${{ secrets.DOCKER_PASS }}
        run: |
          echo $env:DOCKER_PASS | docker login -u $env:DOCKER_USER --password-stdin
          docker push yourrepo/image:${{ github.run_number }}

      - name: Clean Docker share
        continue-on-error: true
        run: Remove-Item -Path "d:\DockerShare\${{ github.run_id }}" -Recurse -Force
