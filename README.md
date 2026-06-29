# 自动录制 (AutoRecorder)

Android 原生 App，录制并回放屏幕点击操作。

## 功能

- 🔴 **录制**：记录用户的点击操作（坐标 + 时间间隔）
- 🏠 **回桌面**：录制时插入"回桌面"动作，使用 `GLOBAL_ACTION_HOME`（稳定可靠）
- ▶ **回放**：按录制顺序自动执行所有操作
- 悬浮控制面板，可拖动，随时操作

## 技术栈

- Kotlin + 原生 Android SDK
- AccessibilityService（录制点击 + 执行手势）
- WindowManager 悬浮窗
- 最低支持 Android 7.0 (API 24)

---

## 获取 APK（GitHub Actions 自动构建）

### 步骤

1. **Fork 或上传本项目到你的 GitHub 仓库**

2. **Push 代码到 main/master 分支**，Actions 自动触发构建

3. **等待约 3-5 分钟**，构建完成后：
   - 进入仓库 → `Actions` 标签页
   - 点击最新的 workflow run
   - 在 `Artifacts` 区域下载 `AutoRecorder-release.apk`

4. **手动触发构建**（可选）：
   - `Actions` → `Build APK` → `Run workflow`

---

## 使用说明

### 首次设置（需要两个权限）

1. 安装 APK 后打开 App
2. 点击 **「① 开启无障碍服务」** → 在系统设置中找到「自动录制」并开启
3. 点击 **「② 开启悬浮窗权限」** → 允许显示在其他应用上层
4. 点击 **「③ 启动悬浮控制面板」** → 悬浮按钮出现在屏幕顶部

### 日常使用

```
🔴录制  →  操作手机（点击各种界面）  →  ⏹停止  →  ▶回放
```

- 录制过程中点 **🏠桌面** 会把"回桌面"动作插入脚本，同时跳回桌面继续录
- 回放时会严格还原每次操作的时间间隔
- 脚本自动保存，重启 App 后仍可回放

### 注意事项

- 密码框、支付页、系统安全弹窗可能因系统限制无法回放
- 本 App 仅供个人本机使用
- 无障碍服务需用户手动开启，这是 Android 系统安全要求

---

## 项目结构

```
app/src/main/
├── kotlin/com/autorecorder/
│   ├── MainActivity.kt              # 权限引导界面
│   ├── AutoAccessibilityService.kt  # 录制 + 回放核心
│   ├── FloatingControlService.kt    # 悬浮控制面板
│   ├── ActionStep.kt                # 动作数据模型
│   └── ScriptStore.kt               # 脚本存储
├── res/
│   ├── xml/accessibility_config.xml
│   └── layout/activity_main.xml
└── AndroidManifest.xml
```
