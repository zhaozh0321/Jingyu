# 构建与运行

## Android Studio / Gradle

需要 JDK 17、Android SDK Platform 35 和 Build Tools 35.0.0。打开根目录，等待 Gradle 同步。首次构建需要访问 Google Maven、Maven Central 与 Gradle 官方发行站。

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

输出在 `app/build/outputs/apk/debug/`。Debug 应用 ID 带 `.debug`，可与正式版共存。请在应用设置中自行填写 API Key，构建不需要真实 API Key。

工具链固定为 AGP 8.9.2、Gradle 8.11.1、JDK 17。Wrapper JAR 和发行包校验值来自 Gradle 官方校验页：https://gradle.org/release-checksums/ 。这条路径已在 GitHub Actions 的 `Android checks` 工作流中实跑通过，日志与产物见 https://github.com/zhaozh0321/Jingyu/actions/workflows/android.yml 。

## 已验证的 PowerShell 轻量路径

工具目录应有 `jdk/<JDK17>/`、`platform/android-35/android.jar`、`build-tools/<版本>/`。不把工具目录提交到仓库。

```powershell
./build.ps1 -ToolsDirectory 'C:/android-tools'
./build-host-tests.ps1 -ToolsDirectory 'C:/android-tools'
./build-stream-tests.ps1 -ToolsDirectory 'C:/android-tools'
```

`build.ps1` 会从 vendored AAR 提取依赖资源与类，生成与签名匹配的 BuildConfig，调用 aapt2/javac/R8，再对齐并签名 APK。默认创建仅供本地开发的新密钥；它不能覆盖已安装的正式版。维护者可用 `-KeystorePath` 指定单独保管的原签名文件。当前轻量脚本的本地测试密钥口令为 `android`，正式发布不要沿用这种口令策略。

## 正式签名

Gradle 的 release 默认输出未签名 APK。签名正式包前设置 `.env.example` 列出的环境变量，尤其是实际证书的 `JINGYU_SIGNING_SHA256`。不要把 `.env` 或 keystore 提交到 Git。详见 RELEASING.md。

## 常见问题

- SDK 路径：用 Android Studio 生成 `local.properties`，或设置 `ANDROID_HOME`。
- 无法下载：先检查官方域名连通性；不要关闭 TLS 验证。
- 无法覆盖安装：检查应用 ID 和签名；不应通过卸载来“修复”而丢失本地数据。
- 字体：不要裁掉 GSUB/GPOS；修改后应核对 shaping、行高、常用字符与许可证。
