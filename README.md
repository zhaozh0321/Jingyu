<div align="center">
  <img src="docs/assets/jingyu-mark.svg" width="88" alt="鲸语标识" />
  <h1>鲸语 · Jingyu</h1>
  <p>简洁、原生、注重阅读体验的 Android DeepSeek 客户端。</p>
  <p><a href="#快速开始">快速开始</a> · <a href="docs/BUILDING.md">构建</a> · <a href="docs/ARCHITECTURE.md">架构</a> · <a href="CHANGELOG.md">更新日志</a> · <a href="CONTRIBUTING.md">参与贡献</a></p>
</div>

## 项目简介

鲸语使用 Java 和 Android 原生控件构建，通过用户自己的 API Key 直连 DeepSeek。当前版本 **2.2.1**，支持 Android 8.0（API 26）及以上。

| 能力 | 实现 |
| --- | --- |
| 对话与阅读 | 流式输出、Markdown、LaTeX、代码高亮、浅色和深色主题 |
| 上下文 | 本地历史、可编辑记忆、按需工具规则、长对话整理 |
| 文件处理 | 附件阅读；Word、PDF、Excel、Markdown、TXT、CSV 生成 |
| 本地工具 | 数学计算、统计、多工作表和原生柱状/折线图 |
| 数据管理 | 历史和记忆备份、文件保存与分享、账户余额查询 |

PPT、HTML、任意代码执行和任意 Office 版式复刻不在当前能力范围内。联网能力取决于服务端工具接口的可用性。

## 快速开始

使用已验证的 APK 时，在应用的“账户与连接”中填写自己的 DeepSeek API Key。API 使用费用由服务商计收，项目不提供共享密钥。

开发者可以直接用 Android Studio 打开项目根目录，选择 `app` 模块的 `debug` 构建：

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Windows 使用 `gradlew.bat`。需要 JDK 17 和 Android SDK 35；Gradle Wrapper 固定为 8.11.1。开发包使用独立应用 ID `cn.jingyu.deepseek.debug`，不会覆盖正式版。

网络受限时，可以沿用已验证的 PowerShell 轻量构建路径，见 [构建文档](docs/BUILDING.md)。Gradle 工程和 CI 配置已补齐，但本次环境无法下载 Gradle/AGP，尚未完成 Gradle 路径实跑，不能视为 CI 已通过。

## 项目结构

| 目录 | 用途 |
| --- | --- |
| `app/src/main/` | 应用源码、原生资源和本地字体 |
| `app/src/test/` | JUnit 回归入口 |
| `test/` | 核心、文件产物、流式逻辑与设备测试源码 |
| `vendor/` | 固定版本依赖及对应源码 JAR |
| `docs/` | 构建、架构、隐私、发布和测试说明 |
| `.github/` | CI、问题模板、PR 模板和依赖更新配置 |

## 验证状态

2.2.1 已有 **117 项主机回归检查**：95 项核心/文件/体验检查，加 22 项帧调度、贴底和原文边界检查。显示时钟与视图替身验证的是逻辑，不能代替 Android 实机帧率测量。本次未完成真机视觉与流畅度验收，详见 [测试说明](docs/TESTING.md)。

## 数据与隐私

API Key 使用 Android Keystore 加密保存；长期记忆使用本地加密存储。会话历史在应用私有目录中以 JSON 保存，并非端到端加密。发送消息、附件内容及相关记忆可能成为发给模型的上下文；手动导出的备份为明文。详见 [隐私说明](docs/PRIVACY.md)。

## 许可证与署名

项目原创代码采用 [Apache-2.0](LICENSE)。第三方代码、库和字体保留各自许可证，详见 [THIRD_PARTY.md](THIRD_PARTY.md)。本项目是独立客户端，与 DeepSeek 官方没有隶属关系。

**Crafted by [@zhaozh0321](https://github.com/zhaozh0321) · with GPT**
