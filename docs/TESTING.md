# 测试与验收

## 已完成

2.2.1 的轻量构建及 APK v2/v3 签名校验已通过。117 项主机检查由 42 项核心、36 项文档/表格、17 项体验逻辑和 22 项流式调度/锚定/原文边界检查组成。Gradle 的单测任务会转发这些检查的输出，`Android checks` 的构建日志因此包含 117 行 `PASS` 和四个套件的 `TOTAL`，可直接核对总数，不必依赖本文的陈述。

```powershell
./build.ps1 -ToolsDirectory 'C:/android-tools'
./build-host-tests.ps1 -ToolsDirectory 'C:/android-tools'
./build-stream-tests.ps1 -ToolsDirectory 'C:/android-tools'
```

Gradle 的 JUnit 入口位于 `app/src/test/`，复用这些产物与逻辑回归。公开证书 DER 只是测试夹具，不是签名私钥。显示时钟和视图替身位于测试源码，绝不打入 APK。

## 尚需验收

Gradle 路径已在 GitHub Actions 实跑：`assembleDebug`、`testDebugUnitTest`、`lintDebug` 均通过，`lintDebug` 为 0 错误，产出 `app-debug.apk`。新增 UI 改动仍未完成 Android 设备截图、帧耗时和卡顿率测量；不能把主机调度检查称为实机性能结果。

设备验收应覆盖：浅/深色；大字号；键盘与旋转；长回答增长与结束；用户上翻时不抢滚动；重连、中断和恢复；代码/公式/表格边界；TalkBack；文件打开、保存和分享。可用 `adb shell dumpsys gfxinfo cn.jingyu.deepseek framestats` 记录真实帧数据。

`test/android/` 保留自定义 Instrumentation 检查源码。包含真实网络的检查需要单独设置临时测试凭据，默认 CI 不访问个人账户，也不需要 API Key。
