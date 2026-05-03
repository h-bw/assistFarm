param(
  # Allowed actions: dev / build / test / backend / user-ui / admin-ui / status / crawl-images
  [Parameter(Mandatory = $true)]
  [ValidateSet('dev', 'build', 'test', 'backend', 'user-ui', 'admin-ui', 'status', 'crawl-images')]
  [string]$Action,
  # 额外参数：转发给子脚本/命令（PowerShell 5.1 兼容做法）
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$Rest,
  # 兼容 PowerShell 5.1：用一个字符串传递给 Node 脚本的参数
  # 示例：-Action crawl-images -NodeArgs "--per 8 --limitProducts 10 --report .downloaded-images\\crawl-report.json"
  [string]$NodeArgs = ''
)

# Enable UTF-8 output
. "$PSScriptRoot\powershell-utf8.ps1"

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-Safe {
  param(
    [Parameter(Mandatory = $true)][string]$WorkingDirectory,
    [Parameter(Mandatory = $true)][string]$Command
  )

  Push-Location $WorkingDirectory
  try {
    Write-Host ''
    Write-Host ('[Run] ' + $WorkingDirectory) -ForegroundColor Cyan
    Write-Host ('> ' + $Command) -ForegroundColor Cyan
    Invoke-Expression $Command
  } finally {
    Pop-Location
  }
}

function Start-Backend {
  $jar = Join-Path $PSScriptRoot '..\huacai-admin\target\huacai-admin.jar'
  $jar = (Resolve-Path $jar).Path
  if (-not (Test-Path $jar)) {
    throw ("Backend jar not found: " + $jar + ". Please run: mvn -pl huacai-admin -am package -DskipTests")
  }
  Write-Host ''
  Write-Host '[Backend] Starting (keep this window open)...' -ForegroundColor Yellow
  & java -jar $jar
}

function Start-UserUI {
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..\huacai-user-ui')).Path -Command 'npm run dev'
}

function Start-AdminUI {
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..\huacai-admin-ui')).Path -Command 'npm run dev'
}

function Build-Frontends {
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..\huacai-user-ui')).Path -Command 'npm run build:prod'
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..\huacai-admin-ui')).Path -Command 'npm run build:prod'
}

function Test-Backend {
  $mvn = 'd:\DevelopSoft\apache-maven-3.9.11\bin\mvn.cmd'
  if (-not (Test-Path $mvn)) {
    throw ('mvn.cmd not found: ' + $mvn + ' (update scripts/run.ps1 to your Maven path)')
  }
  $cmd = $mvn + ' -pl huacai-assisting,huacai-system -am test -DskipITs'
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..')).Path -Command $cmd
}

function Show-Status {
  Write-Host ''
  Write-Host '[Status] Port check' -ForegroundColor Yellow
  '8080','80','90' | ForEach-Object {
    $p = [int]$_
    $ok = (Test-NetConnection -ComputerName 'localhost' -Port $p).TcpTestSucceeded
    $state = 'DOWN'
    if ($ok) { $state = 'OK' }
    Write-Host ('- localhost:{0} => {1}' -f $p, $state)
  }
}

function Crawl-Images {
  $scriptPath = Join-Path $PSScriptRoot 'crawl-product-images.mjs'
  if (-not (Test-Path $scriptPath)) {
    throw ('Script not found: ' + $scriptPath)
  }
  Write-Host ''
  Write-Host '[Images] Crawling images (keep this window open)...' -ForegroundColor Yellow
  # 你可以追加参数，比如：--per 8 --out .downloaded-images --sql sql/huacai-assisting.sql
  # PowerShell 5.1 对未知 -xxx 参数会当成脚本参数报错，因此推荐用 -NodeArgs 传递
  $nodeArgList = @()
  if ($Rest -and $Rest.Count -gt 0) { $nodeArgList += $Rest }
  if ($NodeArgs) {
    $nodeArgList += ($NodeArgs -split '\s+' | Where-Object { $_ -ne '' })
  }
  & node $scriptPath @nodeArgList
}

switch ($Action) {
  'status' { Show-Status }
  'backend' { Start-Backend }
  'user-ui' { Start-UserUI }
  'admin-ui' { Start-AdminUI }
  'build' { Build-Frontends }
  'test' { Test-Backend }
  'crawl-images' { Crawl-Images }
  'dev' {
    Write-Host ''
    Write-Host '[Dev] Open 3 PowerShell windows and run:' -ForegroundColor Yellow
    Write-Host '- Backend:  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action backend'
    Write-Host '- User UI:  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action user-ui'
    Write-Host '- Admin UI: powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action admin-ui'
    Write-Host ''
    Write-Host 'Tip: Vite will switch ports if conflicts occur.'
  }
}

