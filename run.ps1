param(
  # 允许的动作：dev / build / test / backend / user-ui / admin-ui / status / crawl-images
  [Parameter(Mandatory = $true)]
  [ValidateSet('dev', 'build', 'test', 'backend', 'user-ui', 'admin-ui', 'status', 'crawl-images')]
  [string]$Action
)

# 启用 UTF-8（编码）输出，避免中文乱码
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
    Write-Host ''  # 空行分隔输出
    Write-Host ('[Run] ' + $WorkingDirectory) -ForegroundColor Cyan
    Write-Host ('> ' + $Command) -ForegroundColor Cyan
    # 使用 PowerShell 内置调用，避免复杂拼接导致解析错误
    Invoke-Expression $Command
  } finally {
    Pop-Location
  }
}

function Start-Backend {
  $jar = Join-Path $PSScriptRoot '..\huacai-admin\target\huacai-admin.jar'
  $jar = (Resolve-Path $jar).Path
  if (-not (Test-Path $jar)) {
    throw "未找到后端 jar：$jar。请先执行：mvn -pl huacai-admin -am package -DskipTests"
  }
  Write-Host ''
  Write-Host '[Backend] 启动中（保持窗口运行）...' -ForegroundColor Yellow
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
    throw ('未找到 mvn.cmd：' + $mvn + '（请按你的实际 Maven 路径修改 scripts/run.ps1）')
  }
  # Windows PowerShell 5.1 兼容：避免在字符串里拼接调用操作符 &
  # 这里 mvn 路径不含空格，直接拼接参数即可
  $cmd = $mvn + ' -pl huacai-assisting,huacai-system -am test -DskipITs'
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..')).Path -Command $cmd
}

function Show-Status {
  Write-Host ''
  Write-Host '[Status] 端口检查' -ForegroundColor Yellow
  '8080','80','90' | ForEach-Object {
    $p = [int]$_
    $ok = (Test-NetConnection -ComputerName 'localhost' -Port $p).TcpTestSucceeded
    # Windows PowerShell 5.1 不支持三元表达式 ? :
    $state = 'DOWN'
    if ($ok) { $state = 'OK' }
    Write-Host ('- localhost:{0} => {1}' -f $p, $state)
  }
}

function Crawl-Images {
  $scriptPath = Join-Path $PSScriptRoot 'crawl-product-images.mjs'
  if (-not (Test-Path $scriptPath)) {
    throw ('未找到脚本：' + $scriptPath)
  }
  Write-Host ''
  Write-Host '[Images] 开始爬取图片（保持窗口运行）...' -ForegroundColor Yellow
  # 说明：参数可自行追加，比如：--per 8 --out .downloaded-images --sql sql/huacai-assisting.sql
  & node $scriptPath
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
    Write-Host '[Dev] 请分别打开 3 个 PowerShell 窗口运行：' -ForegroundColor Yellow
    Write-Host '- 后端：  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action backend'
    Write-Host '- 用户端：powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action user-ui'
    Write-Host '- 管理端：powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action admin-ui'
    Write-Host ''
    Write-Host '提示：前端端口冲突时 Vite 会自动换端口。'
  }
}

