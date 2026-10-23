param([string]$ToolsDirectory=(Join-Path $PSScriptRoot '..\android-tools'))
$ErrorActionPreference='Stop'
[Console]::OutputEncoding=[Text.Encoding]::UTF8
$project=$PSScriptRoot
$jdk=(Get-ChildItem (Join-Path $ToolsDirectory 'jdk') -Directory | Select-Object -First 1).FullName
$platform=(Get-ChildItem (Join-Path $ToolsDirectory 'platform') -Directory | Select-Object -First 1).FullName
$out=Join-Path $project 'build-host'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$json=@(Get-ChildItem (Join-Path $project 'test\host-json\org\json') -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -d $out @json
if($LASTEXITCODE -ne 0){throw 'Host JSON compile failed'}
$libs=@(Get-ChildItem (Join-Path $project 'vendor') -Filter '*.jar' | Where-Object Name -NotLike '*sources*' | ForEach-Object FullName)
$cp=$out+';'+(Join-Path $project 'build-final-native\classes')+';'+(Join-Path $platform 'android.jar')+';'+($libs -join ';')
& (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -cp $cp -d $out (Join-Path $project 'test\HostQualityTest.java') (Join-Path $project 'test\PublishingQualityTest.java') (Join-Path $project 'test\ExperienceQualityTest.java')
if($LASTEXITCODE -ne 0){throw 'Quality tests compile failed'}
& (Join-Path $jdk 'bin\java.exe') -cp $cp cn.jingyu.deepseek.PublishingQualityTest (Join-Path $out 'samples') (Join-Path $project 'build-final-native\signer.der') | Tee-Object -FilePath (Join-Path $project 'build-host\publishing-report.txt')
if($LASTEXITCODE -ne 0){throw 'Quality regression failed'}
& (Join-Path $jdk 'bin\java.exe') -cp $cp cn.jingyu.deepseek.ExperienceQualityTest | Tee-Object -FilePath (Join-Path $project 'build-host\experience-report.txt')
if($LASTEXITCODE -ne 0){throw 'Experience regression failed'}
