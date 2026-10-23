param([string]$ToolsDirectory=(Join-Path $PSScriptRoot '..\android-tools'))
$ErrorActionPreference='Stop'
$jdk=(Get-ChildItem (Join-Path $ToolsDirectory 'jdk') -Directory | Select-Object -First 1).FullName
$out=Join-Path $PSScriptRoot 'build-stream-host'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$fake=@(Get-ChildItem (Join-Path $PSScriptRoot 'test\stream-host') -Filter '*.java' -Recurse | ForEach-Object FullName)
& (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -cp (Join-Path $PSScriptRoot 'build-final-native\classes') -d $out @fake (Join-Path $PSScriptRoot 'test\StreamQualityTest.java')
if($LASTEXITCODE -ne 0){throw 'Stream regression compile failed'}
$cp=$out+';'+(Join-Path $PSScriptRoot 'build-final-native\classes')
& (Join-Path $jdk 'bin\java.exe') -cp $cp cn.jingyu.deepseek.StreamQualityTest | Tee-Object -FilePath (Join-Path $PSScriptRoot 'build-stream-host\stream-report.txt')
if($LASTEXITCODE -ne 0){throw 'Stream regression failed'}
