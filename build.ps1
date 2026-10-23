param([string]$ToolsDirectory=(Join-Path $PSScriptRoot '..\android-tools'),[string]$KeystorePath='')
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
$jdk=(Get-ChildItem -LiteralPath (Join-Path $ToolsDirectory 'jdk') -Directory | Select-Object -First 1).FullName
$platform=(Get-ChildItem -LiteralPath (Join-Path $ToolsDirectory 'platform') -Directory | Select-Object -First 1).FullName
$buildTools=(Get-ChildItem -LiteralPath (Join-Path $ToolsDirectory 'build-tools') -Directory | Select-Object -First 1).FullName
$androidJar=Join-Path $platform 'android.jar'
python (Join-Path $project 'prepare_vendor.py')
if($LASTEXITCODE -ne 0){throw 'Vendor preparation failed'}
$libs=@(Get-ChildItem (Join-Path $project 'vendor') -Filter '*.jar' | Where-Object {$_.Name -notlike '*sources*'} | ForEach-Object FullName)
$classPath=$androidJar+';'+($libs -join ';')
$env:JAVA_HOME=$jdk
$env:PATH=(Join-Path $jdk 'bin')+';'+$env:PATH
$out=Join-Path $project 'build-final-native'
foreach($folder in @('classes','dex','generated')){
 $target=[IO.Path]::GetFullPath((Join-Path $out $folder))
 if(-not $target.StartsWith([IO.Path]::GetFullPath($out)+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe build output path'}
 if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target -Recurse -Force}
}
New-Item -ItemType Directory -Path $out,(Join-Path $out 'classes'),(Join-Path $out 'dex') -Force | Out-Null
function Run-Tool { param([string]$Exe,[string[]]$Arguments) & $Exe @Arguments; if($LASTEXITCODE -ne 0){throw "Tool failed: $Exe ($LASTEXITCODE)"} }
$signing=Join-Path $project '.signing'
New-Item -ItemType Directory -Path $signing -Force | Out-Null
$keystore=if($KeystorePath){[IO.Path]::GetFullPath($KeystorePath)}else{Join-Path $signing 'jingyu.keystore'}
if(!(Test-Path $keystore)){Run-Tool (Join-Path $jdk 'bin\keytool.exe') @('-genkeypair','-keystore',$keystore,'-alias','jingyu','-storepass','android','-keypass','android','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Jingyu Local Build,O=Personal,C=CN')}
$certificate=Join-Path $out 'signer.der'
Run-Tool (Join-Path $jdk 'bin\keytool.exe') @('-exportcert','-keystore',$keystore,'-storepass','android','-alias','jingyu','-file',$certificate)
$fingerprint=(Get-FileHash -LiteralPath $certificate -Algorithm SHA256).Hash.ToLowerInvariant()
$configDirectory=Join-Path $out 'generated\cn\jingyu\deepseek'
New-Item -ItemType Directory -Path $configDirectory -Force | Out-Null
$config='package cn.jingyu.deepseek; public final class BuildConfig {public static final boolean DEBUG=false;public static final String SIGNING_CERT_SHA256="'+$fingerprint+'";}'
[IO.File]::WriteAllText((Join-Path $configDirectory 'BuildConfig.java'),$config,[Text.UTF8Encoding]::new($false))
$manifestPath=Join-Path $out 'AndroidManifest.xml'
$manifest=[IO.File]::ReadAllText((Join-Path $project 'app\src\main\AndroidManifest.xml'))
$versionName=([Regex]::Match([IO.File]::ReadAllText((Join-Path $project 'gradle.properties')),'(?m)^jingyuVersionName=(.+)$')).Groups[1].Value.Trim()
$versionCode=([Regex]::Match([IO.File]::ReadAllText((Join-Path $project 'gradle.properties')),'(?m)^jingyuVersionCode=(\d+)$')).Groups[1].Value
$manifest=$manifest.Replace('${applicationId}','cn.jingyu.deepseek').Replace('<manifest ',('<manifest package="cn.jingyu.deepseek" android:versionCode="'+$versionCode+'" android:versionName="'+$versionName+'" '))
[IO.File]::WriteAllText($manifestPath,$manifest,[Text.UTF8Encoding]::new($false))
Run-Tool (Join-Path $buildTools 'aapt2.exe') @('compile','--dir',(Join-Path $project 'app\src\main\res'),'-o',(Join-Path $out 'resources.zip'))
Run-Tool (Join-Path $buildTools 'aapt2.exe') @('link','-o',(Join-Path $out 'unsigned.apk'),'-0','xml','-0','ttf','--java',(Join-Path $out 'generated'),'--extra-packages','io.noties.markwon:io.noties.markwon.ext.tables:ru.noties.jlatexmath','--manifest',$manifestPath,'-I',$androidJar,'-A',(Join-Path $project 'app\src\main\assets'),(Join-Path $out 'resources.zip'))
$sources=@(Get-ChildItem (Join-Path $project 'app\src\main\java'),(Join-Path $out 'generated') -Recurse -Filter '*.java' | ForEach-Object FullName)
Run-Tool (Join-Path $jdk 'bin\javac.exe') (@('-encoding','UTF-8','-source','8','-target','8','-classpath',$classPath,'-d',(Join-Path $out 'classes'))+$sources)
$classFiles=@(Get-ChildItem (Join-Path $out 'classes') -Recurse -Filter '*.class' | ForEach-Object FullName)
$d8ArgsFile=Join-Path $out 'd8-args.txt'
$d8Args=@('--min-api','26','--lib',$androidJar,'--output',(Join-Path $out 'dex'))+$classFiles+$libs
[IO.File]::WriteAllLines($d8ArgsFile,($d8Args | ForEach-Object {$_.Replace('\','/')}),[Text.UTF8Encoding]::new($false))
$r8Args=@('--release','--lib',$androidJar,'--pg-conf',(Join-Path $project 'app\proguard-rules.pro'),'--pg-map-output',(Join-Path $out 'mapping.txt'),'--output',(Join-Path $out 'dex'))+$classFiles+$libs
$r8ArgsFile=Join-Path $out 'r8-args.txt'
[IO.File]::WriteAllLines($r8ArgsFile,($r8Args | ForEach-Object {$_.Replace('\','/')}),[Text.UTF8Encoding]::new($false))
Run-Tool (Join-Path $jdk 'bin\java.exe') @('-cp',(Join-Path $buildTools 'lib\d8.jar'),'com.android.tools.r8.R8',('@'+$r8ArgsFile))
python (Join-Path $project 'repack_apk.py') (Join-Path $out 'unsigned.apk') (Join-Path $out 'dex\classes.dex')
if($LASTEXITCODE -ne 0){throw 'APK packaging failed'}
Run-Tool (Join-Path $buildTools 'zipalign.exe') @('-f','-p','4',(Join-Path $out 'unsigned.apk'),(Join-Path $out 'aligned.apk'))
$apk=Join-Path $project ('Jingyu-'+$versionName+'.apk')
Run-Tool (Join-Path $buildTools 'apksigner.bat') @('sign','--ks',$keystore,'--ks-key-alias','jingyu','--ks-pass','pass:android','--key-pass','pass:android','--out',$apk,(Join-Path $out 'aligned.apk'))
Run-Tool (Join-Path $buildTools 'apksigner.bat') @('verify','--verbose',$apk)
Write-Output "Built: $apk"
