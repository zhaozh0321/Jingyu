# 发布流程

1. 更新 `gradle.properties` 的版本号与版本代码，以及 CHANGELOG。
2. 运行逻辑回归和 lint，完成设备视觉、长回答与数据升级验收。
3. 在受控环境提供签名私钥和对应证书 SHA-256；私钥、口令及真实 API Key 不进入仓库。
4. 用原签名构建正式包，核对应用 ID、版本、签名、许可证、文件体积及敏感信息扫描。
5. 创建 `v<版本>` 标签和 GitHub Release，附 APK、SHA-256 校验值、变更与已知限制。不要把 APK 当源码放进 Git 历史。

环境变量模板见 `.env.example`。Gradle `assembleRelease` 只有在设置签名参数后才输出可分发的签名包；CI 默认仅构建独立开发包。开发包不能覆盖正式版，正式版升级必须沿用原签名。

```bash
./gradlew :app:assembleRelease
sha256sum app/build/outputs/apk/release/*.apk
```

首个仓库导入不代表所有验收已完成；请在 Release 中如实记录未验证范围。
