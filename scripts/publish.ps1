param([string]$Repository='zhaozh0321/Jingyu',[switch]$Public)
$ErrorActionPreference='Stop'
$project=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $project
if(!(Get-Command gh -ErrorAction SilentlyContinue)){throw 'Install GitHub CLI from https://cli.github.com/ and run gh auth login first.'}
& gh auth status
if($LASTEXITCODE -ne 0){throw 'Sign in with gh auth login first.'}
python (Join-Path $PSScriptRoot 'check_project.py')
if($LASTEXITCODE -ne 0){throw 'Project checks failed; nothing uploaded.'}
if(!(Test-Path -LiteralPath (Join-Path $project '.git'))){
 & git init -b main
 if($LASTEXITCODE -ne 0){throw 'Could not initialize Git repository.'}
 & git add --all
 & git -c user.name=zhaozh0321 -c user.email=275944874+zhaozh0321@users.noreply.github.com commit -m 'Import Jingyu 2.2.1 Android project'
 if($LASTEXITCODE -ne 0){throw 'Could not commit prepared project.'}
}
& gh auth setup-git --hostname github.com
if($LASTEXITCODE -ne 0){throw 'Could not configure GitHub Git authentication.'}
$remote=& git remote get-url origin 2>$null
if($LASTEXITCODE -eq 0){
 $expected='https://github.com/'+$Repository+'.git'
 if($remote -ne $expected){throw 'Existing origin differs from the requested repository; inspect before pushing.'}
 & git push -u origin main
}else{
 $visibility=if($Public){'--public'}else{'--private'}
 & gh repo create $Repository $visibility --description '鲸语：原生 Android DeepSeek 客户端，支持流式阅读、记忆和本地文档工具' --source . --remote origin --push
}
if($LASTEXITCODE -ne 0){throw 'GitHub upload did not complete.'}
Write-Output ('Uploaded: https://github.com/'+$Repository)
