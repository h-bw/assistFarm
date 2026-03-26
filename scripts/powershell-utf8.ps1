$utf8 = [System.Text.UTF8Encoding]::new($false)

chcp 65001 > $null

[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8

$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'
$PSDefaultParameterValues['Set-Content:Encoding'] = 'utf8'
$PSDefaultParameterValues['Add-Content:Encoding'] = 'utf8'
$PSDefaultParameterValues['Export-Csv:Encoding'] = 'utf8'

[System.Environment]::SetEnvironmentVariable('PYTHONIOENCODING', 'utf-8', 'Process')

Write-Host 'PowerShell UTF-8 environment enabled.' -ForegroundColor Green
Write-Host ('CodePage: ' + [Console]::OutputEncoding.CodePage) -ForegroundColor DarkGreen
