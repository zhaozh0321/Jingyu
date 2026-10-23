# 上传到 GitHub

源码仓库已发布在 <https://github.com/zhaozh0321/Jingyu>（公开，默认分支 `main`）。项目以正常源码树提交，248 个文件，没有把源码压缩包或 APK 当作仓库内容。

本文档用于两种情况：把项目重新上传到你自己的账号，或在 fork 中重建仓库。

## 使用附带脚本

安装 Git 和 [GitHub CLI](https://cli.github.com/)，解压项目，进入 `Jingyu` 目录后运行：

```powershell
gh auth login --web
./scripts/publish.ps1
```

脚本默认创建私有仓库 `zhaozh0321/Jingyu`，进行检查、初始化本地提交并推送 `main`。如果明确希望公开，使用：

```powershell
./scripts/publish.ps1 -Public
```

仓库名也可以通过 `-Repository '你的账号/仓库名'` 指定。脚本不会强制覆盖已有的远端分支。

推送包含 `.github/workflows/` 的提交时，令牌必须带 `workflow` 权限，否则 GitHub 会拒绝整个推送。用 GitHub CLI 登录时可以一次声明全部所需范围：

```powershell
gh auth login --web --scopes "repo,workflow,read:org,gist"
```

## 已有仓库

先确认远端没有需要保留的提交。若远端只有占位 README，可把本地提交接在它之后，以快进方式推送，避免强推：

```powershell
git init -b main
git add --all
git commit -m "Import Jingyu 2.2.1 Android project"
git remote add origin https://github.com/zhaozh0321/Jingyu.git
git fetch origin main
```

然后以远端最新提交为父提交重建你的提交：

```bash
TREE=$(git rev-parse 'HEAD^{tree}')
NEW=$(git commit-tree "$TREE" -p FETCH_HEAD -m "Import Jingyu 2.2.1 Android project")
git update-ref refs/heads/main "$NEW"
git push -u origin main
```

首次提交需要配置自己的 Git 姓名和邮箱。可使用 GitHub 提供的 noreply 邮箱。不要把签名私钥、口令或 API Key 放进仓库；正式 APK 应附在 Release 中。

## 中文编码

仓库所有文本文件为 UTF-8。上传后若首页中文显示为方块或问号，通常是文件在传输中被按本地代码页转换，中文会变成 `U+FFFD` 替换字符（字节 `EF BF BD`）且无法还原，只能重新提交正确编码的文件。校验方法：

```bash
git show HEAD:README.md | grep -c $'\xef\xbf\xbd'
```

输出应为 `0`。
