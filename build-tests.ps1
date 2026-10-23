param([string]$ToolsDirectory=(Join-Path $PSScriptRoot '..\android-tools'))
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
$tools=$ToolsDirectory
$jdk=(Get-ChildItem (Join-Path $tools 'jdk') -Directory | Select-Object -First 1).FullName
$bt=(Get-ChildItem (Join-Path $tools 'build-tools') -Directory | Select-Object -First 1).FullName
$platform=(Get-ChildItem (Join-Path $tools 'platform') -Directory | Select-Object -First 1).FullName
$jar=Join-Path $platform 'android.jar'
$env:JAVA_HOME=$jdk;$env:PATH=(Join-Path $jdk 'bin')+';'+$env:PATH
$libs=@(Get-ChildItem (Join-Path $project 'vendor') -Filter '*.jar' | Where-Object {$_.Name -notlike '*sources*'} | ForEach-Object FullName)
$testClasspath=$jar+';'+(Join-Path $project 'build-final-native\classes')+';'+($libs -join ';')
$out=Join-Path $project 'build-instrumentation'
foreach($folder in @('classes','dex')){
 $target=[IO.Path]::GetFullPath((Join-Path $out $folder))
 if(-not $target.StartsWith([IO.Path]::GetFullPath($out)+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe test output path'}
 if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target -Recurse -Force}
}
New-Item -ItemType Directory -Path $out,(Join-Path $out 'classes'),(Join-Path $out 'dex') -Force | Out-Null
function Run {param([string]$exe,[string[]]$arguments) & $exe @arguments;if($LASTEXITCODE -ne 0){throw "$exe failed: $LASTEXITCODE"}}
$manifest=Get-Content -Raw -Encoding UTF8 (Join-Path $project 'build-final-native\AndroidManifest.xml')
$manifest=$manifest.Replace('<application ','<application android:debuggable="true" ')
Set-Content -LiteralPath (Join-Path $out 'debug-manifest.xml') -Value $manifest -Encoding utf8
Run (Join-Path $bt 'aapt2.exe') @('link','-o',(Join-Path $out 'debug-unsigned.apk'),'--manifest',(Join-Path $out 'debug-manifest.xml'),'-I',$jar,'-A',(Join-Path $project 'app\src\main\assets'),(Join-Path $project 'build-final-native\resources.zip'))
Add-Type -AssemblyName System.IO.Compression; Add-Type -AssemblyName System.IO.Compression.FileSystem
function Add-Dex {param([string]$apk,[string]$dex) python (Join-Path $project 'repack_apk.py') $apk $dex; if($LASTEXITCODE -ne 0){throw 'Test packaging failed'}}
$appDex=Join-Path $out 'app-dex'
New-Item -ItemType Directory -Path $appDex -Force | Out-Null
$appClasses=@(Get-ChildItem (Join-Path $project 'build-final-native\classes') -Recurse -Filter '*.class' | ForEach-Object FullName)
$argsFile=Join-Path $out 'app-d8-args.txt'
[IO.File]::WriteAllLines($argsFile,(@('--min-api','26','--lib',$jar,'--output',$appDex)+$appClasses+$libs | ForEach-Object {$_.Replace('\','/')}),[Text.UTF8Encoding]::new($false))
Run (Join-Path $bt 'd8.bat') @('@'+$argsFile)
Add-Dex (Join-Path $out 'debug-unsigned.apk') (Join-Path $appDex 'classes.dex')
Run (Join-Path $jdk 'bin\javac.exe') (@('-encoding','UTF-8','-source','8','-target','8','-classpath',$testClasspath,'-d',(Join-Path $out 'classes'))+@(Get-ChildItem (Join-Path $project 'test\android') -Filter '*.java' | ForEach-Object FullName))
$classes=@(Get-ChildItem (Join-Path $out 'classes') -Recurse -Filter '*.class' | ForEach-Object FullName)
Run (Join-Path $bt 'd8.bat') (@('--min-api','26','--lib',$jar,'--classpath',(Join-Path $project 'build-final-native\classes'),'--output',(Join-Path $out 'dex'))+$classes)
Run (Join-Path $bt 'aapt2.exe') @('link','-o',(Join-Path $out 'tests-unsigned.apk'),'--manifest',(Join-Path $project 'test\android\AndroidManifest.xml'),'-I',$jar)
Add-Dex (Join-Path $out 'tests-unsigned.apk') (Join-Path $out 'dex\classes.dex')
foreach($name in @('debug','tests')){Run (Join-Path $bt 'zipalign.exe') @('-f','-p','4',(Join-Path $out ($name+'-unsigned.apk')),(Join-Path $out ($name+'-aligned.apk')));Run (Join-Path $bt 'apksigner.bat') @('sign','--ks',(Join-Path $project '.signing\jingyu.keystore'),'--ks-key-alias','jingyu','--ks-pass','pass:android','--key-pass','pass:android','--out',(Join-Path $out ($name+'.apk')),(Join-Path $out ($name+'-aligned.apk')))}
