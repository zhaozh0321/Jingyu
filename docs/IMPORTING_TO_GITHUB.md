# 上传到 GitHub

项目已整理为正常源码树，包含初始提交所需的全部文件；没有把源码压缩包或 APK 当作仓库内容。

当前自动化环境无法读取 Git 登录凭据，也没有可用的浏览器会话，GitHub 连接不能创建仓库，因此远端上传没有完成。以下步骤应在你自己的终端执行。

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

## 已有空仓库

确认仓库属于自己且没有需要保留的内容后，可以手动设置远端并推送：

```powershell
git init -b main
git add --all
git commit -m "Import Jingyu 2.2.1 Android project"
git remote add origin https://github.com/zhaozh0321/Jingyu.git
git push -u origin main
```

首次提交需要配置自己的 Git 姓名和邮箱。可使用 GitHub 提供的 noreply 邮箱。不要把签名私钥、口令或 API Key 放进仓库；正式 APK 应附在 Release 中。
