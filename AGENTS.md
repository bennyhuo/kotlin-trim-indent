# Kotlin-Trim-Indent 维护与 Kotlin 版本升级指南

本文档记录了 `Kotlin-Trim-Indent` 编译器插件在升级和适配新 Kotlin 版本时的标准流程、版本发布策略及注意事项。

## 1. 版本发布策略

- **大版本升级（`x.y.0`）**：每个 Kotlin 大版本（如 `2.2.0`、`2.3.0`、`2.4.0`）通常都需要进行适配并发布对应的正式版本（版本号格式为 `<kotlin-version>-<plugin-version>`，例如 `2.4.0-1.1.0`）。
- **增量版本（`x.y.20`）**：
  - 先验证已发布的 `x.y.0` 插件是否可以直接兼容 `x.y.20`。
  - **可直接兼容**：如果 `sample` 工程切换到 `x.y.20` 后能够正常编译且运行结果正确，则**无需**针对 `x.y.20` 单独发版。
  - **不兼容**：如果编译器 API 或 IR 结构在 `x.y.20` 发生破坏性变更导致不兼容，则专门针对 `x.y.20` 进行适配并发布 `x.y.20-<plugin-version>` 正式版。

---

## 2. 升级与适配步骤（以升级 `x.y.0` 为例）

### 2.1 检查前置依赖

升级前先确认 Maven Central 上以下依赖的目标 Kotlin 版本已发布：
1. `org.jetbrains.kotlin:kotlin-stdlib:<kotlin-version>`
   - 元数据地址：`https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/maven-metadata.xml`
2. `com.bennyhuo.kotlin:kotlin-compile-testing-extensions:<kotlin-version>-<ext-version>`
   - 元数据地址：`https://repo1.maven.org/maven2/com/bennyhuo/kotlin/kotlin-compile-testing-extensions/maven-metadata.xml`

### 2.2 更新版本配置

依次更新以下 4 个文件中的版本号：

1. **[build.gradle.kts](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/build.gradle.kts)**：
   ```kotlin
   plugins {
       kotlin("jvm") version "<kotlin-version>" apply false
       ...
   }
   ```
2. **[gradle.properties](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/gradle.properties)**：
   ```properties
   VERSION_NAME=<kotlin-version>-1.1.0
   ```
3. **[trimindent-compiler/build.gradle.kts](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/build.gradle.kts)**：
   ```kotlin
   testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:<kotlin-version>-<ext-version>")
   ```
4. **[sample/gradle/libs.versions.toml](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/sample/gradle/libs.versions.toml)**：
   ```toml
   [versions]
   kotlin = "<kotlin-version>"
   ```

### 2.3 编译器插件与测试代码适配

执行编译并根据编译器报错或废弃警告重点检查以下位置：

- **[TrimIndentCompilerPluginRegistrar.kt](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/src/main/java/com/bennyhuo/kotlin/trimindent/compiler/TrimIndentCompilerPluginRegistrar.kt)**：
  - 检查 `CompilerPluginRegistrar` 抽象成员变更（如 Kotlin 2.4 新增必填属性 `override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID`）。
  - 检查 `CompilerConfiguration` keys（如 `CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY`）及可空性变更。
- **[IrUtils.kt](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/src/main/java/com/bennyhuo/kotlin/trimindent/compiler/IrUtils.kt) / [String.kt](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/src/main/java/com/bennyhuo/kotlin/trimindent/compiler/String.kt) / [TrimIndentIrGenerator.kt](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/src/main/java/com/bennyhuo/kotlin/trimindent/compiler/TrimIndentIrGenerator.kt)**：
  - 检查 `IrPluginContext` 符号查找 API（如使用 `finderForBuiltins().findFunctions(...)` 替代废弃的 `referenceFunctions(...)`）。
  - 检查 `IrCall` / `IrFunction` 的参数列表与 Receiver API（如 `arguments`、`parameters`）。
  - 检查新增的 OptIn 要求（如 `@OptIn(UnsafeDuringIrConstructionAPI::class)`）。
- **[TrimIndentTest.kt](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/trimindent-compiler/src/test/java/com/bennyhuo/kotlin/trimindent/compiler/TrimIndentTest.kt)**：
  - 检查 `kotlin-compile-testing-extensions` 的 `KotlinModule` 构造函数及 `checkResult` 签名变更（如 Kotlin 2.4 移除了 `useK2` 参数）。

---

## 3. 构建与测试验证

> 注意：仓库根目录下 `gradlew` 默认可能没有可执行权限，可使用 `sh ./gradlew` 执行。

1. **运行编译器插件单元测试**：
   ```bash
   sh ./gradlew check
   ```
   确保 `trimindent-compiler:test` 中的全部测试用例（`basic`、`reIndent`、`blankLine`、`embedded`）编译、IR 产物校验与标准输出校验均通过。

2. **验证 `sample` 多平台示例工程**：
   在 `sample/` 目录下运行构建：
   ```bash
   sh ../gradlew build
   ```
   确认 JVM 和 Native（`macosArm64`）目标均能正常编译与链接，并可运行 Native 产物验证输出：
   ```bash
   ./sample/multiplatform-app/build/bin/macosArm64/debugExecutable/multiplatform-app.kexe
   ```

---

## 4. 提交与发布流程

> **重要**：**不要在本地直接运行 `./gradlew publishToMavenCentral` 发布**。正式版发布统一通过推送 Git Tag 触发 GitHub Actions 完成。

1. **同步远端分支**：
   先拉取并 rebase 远端 `origin/master`（因为仓库配置了 `.github/workflows/upgrade-kotlin.yml` 定时任务，远端可能有新提交）：
   ```bash
   git pull --rebase origin master
   ```
2. **提交代码并打 Tag**：
   ```bash
   git commit -am "Bump Kotlin to <kotlin-version>."
   git tag v<VERSION_NAME>
   ```
   例如：`Bump Kotlin to 2.4.0.`，Tag 为 `v2.4.0-1.1.0`。
3. **推送分支与 Tag 触发发布**：
   ```bash
   git push origin master v<VERSION_NAME>
   ```
   推送 Tag `v*` 后会自动触发：
   - [.github/workflows/publish.yml](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/.github/workflows/publish.yml)：调用 [.github/scripts/publish.sh](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/.github/scripts/publish.sh) 执行 `./gradlew publishAndReleaseToMavenCentral` 发布到 Maven Central Portal。
   - [.github/workflows/release.yml](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/.github/workflows/release.yml)：自动创建 GitHub Release。

---

## 5. 验证 `x.y.20` 兼容性流程

当需要确认当前 `x.y.0` 插件版本是否支持 `x.y.20` 时：

1. 保持主工程版本配置为 `x.y.0` 不变，仅将 [sample/gradle/libs.versions.toml](file:///Users/benny/WorkSpace/external/Kotlin-Trim-Indent/sample/gradle/libs.versions.toml) 中的 `kotlin` 版本修改为 `x.y.20`。
2. 在 `sample/` 目录下执行 `sh ../gradlew build`，并运行生成的二进制产物核对输出。
3. **结果处理**：
   - **验证通过**：将 `sample/gradle/libs.versions.toml` 还原为 `x.y.0`，保持工作区干净，无需发版。
   - **验证失败**：按照第 2～4 节流程将主工程升级至 `x.y.20`，完成代码适配后提交并打 Tag `v<x.y.20>-1.1.0` 发布新版本。
