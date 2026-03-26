param(
  # 允许的动作：dev / build / test / backend / user-ui / admin-ui / status
  [Parameter(Mandatory = $true)]
  [ValidateSet('dev', 'build', 'test', 'backend', 'user-ui', 'admin-ui', 'status')]
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
    Write-Host ("`n[Run] " + $WorkingDirectory + "`n> " + $Command) -ForegroundColor Cyan
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
  Write-Host "`n[Backend] 启动中（保持窗口运行）..." -ForegroundColor Yellow
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
    throw "未找到 mvn.cmd：$mvn（请按你的实际 Maven 路径修改 scripts/run.ps1）"
  }
  Invoke-Safe -WorkingDirectory (Resolve-Path (Join-Path $PSScriptRoot '..')).Path -Command ("& '" + $mvn + "' -pl huacai-assisting,huacai-system -am test -DskipITs")
}

function Show-Status {
  Write-Host "`n[Status] 端口检查" -ForegroundColor Yellow
  '8080','80','90' | ForEach-Object {
    $p = [int]$_
    $ok = (Test-NetConnection -ComputerName 'localhost' -Port $p).TcpTestSucceeded
    Write-Host ("- localhost:{0} => {1}" -f $p, ($ok ? 'OK' : 'DOWN'))
  }
}

switch ($Action) {
  'status' { Show-Status }
  'backend' { Start-Backend }
  'user-ui' { Start-UserUI }
  'admin-ui' { Start-AdminUI }
  'build' { Build-Frontends }
  'test' { Test-Backend }
  'dev' {
    Write-Host "`n[Dev] 请分别打开 3 个 PowerShell 窗口运行：" -ForegroundColor Yellow
    Write-Host "- 后端：  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action backend"
    Write-Host "- 用户端：powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action user-ui"
    Write-Host "- 管理端：powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action admin-ui"
    Write-Host "`n提示：前端端口冲突时 Vite 会自动换端口。"
  }
}

