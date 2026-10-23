# 第三方组件

| 组件 | 版本 / 来源 | 许可与位置 |
| --- | --- | --- |
| Markwon core/inline-parser/tables/latex | 4.6.2，https://github.com/noties/Markwon | Apache-2.0；本地 AAR 和源码 JAR，assets/licenses/Markwon.txt |
| CommonMark Java 与 GFM tables | 0.13.0，https://github.com/commonmark/commonmark-java | BSD-2-Clause；本地 JAR、源码和 CommonMark.txt |
| AndroidX Annotation | 1.1.0 | Apache-2.0；AndroidX.txt |
| JLaTeXMath Android | 0.2.0，https://github.com/noties/jlatexmath-android | GPL-2.0-or-later，附独立模块链接例外；保留 COPYING、例外、字体许可和完整上游源码 JAR |
| Noto Serif SC | 用户提供的 wght=520 静态子集 | SIL OFL 1.1；NotoSerifSC.txt，保留 GSUB/GPOS，不单独出售字体 |
| exp4j | commit 4657309f9cd95ca8b6e5e1ef855167d4f9d21d83，https://github.com/fasseg/exp4j | Apache-2.0；app/src/main/java/net/objecthunter/exp4j/，保留原文件头 |
| JSON-java 测试源码 | test/host-json/，https://github.com/stleary/JSON-java | 原源码标注 Public Domain；仅测试使用 |
| Gradle Wrapper | 8.11.1，https://github.com/gradle/gradle/tree/v8.11.1 | Apache-2.0；脚本文件头与 JAR 内许可保留 |

许可证的实际路径为 `app/src/main/assets/licenses/`；JLaTeXMath 字体许可还位于 `app/src/main/assets/org/scilab/forge/jlatexmath/fonts/licences/`。应用的开源许可页面可查看相关文本。原始依赖与对应来源源码位于 `vendor/`，哈希清单为 `vendor/SHA256SUMS`。

JLaTeXMath 的链接例外不免除对该库自身许可和源码的保留义务，项目未修改该库。所有第三方组件继续适用各自许可，根 LICENSE 不覆盖它们。
