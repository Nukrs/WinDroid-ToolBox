# WinDroid Toolbox

一个功能强大的Android刷机工具箱，使用Kotlin Multiplatform和Jetpack Compose for Desktop构建。

## 功能特性

### 🔧 刷机工具
- **Fastboot刷机**: 支持刷入系统镜像、Recovery、Bootloader等
- **ADB工具**: Android调试桥，用于设备调试和文件传输

### 📱 设备管理
- 自动检测连接的Android设备
- 实时显示设备信息（型号、制造商、Android版本等）
- 设备状态监控（Bootloader状态、Root状态等）
- 高级重启模式

### 🎨 用户界面
- 现代化界面
- 支持深色/浅色主题
- 响应式布局设计
- 实时操作日志显示

## 技术栈

- **Kotlin Multiplatform**: 跨平台开发
- **Jetpack Compose for Desktop**: 现代化UI框架
- **Material Design 3**: 设计系统

## 系统要求

- Windows 10/11 (x64)
- Java 17+

## 安装使用

### 从源码构建

1. 克隆仓库
```bash
git clone https://github.com/Nukrs/WinDroid-ToolBox.git
cd WinDroid-ToolBox
```

2. 构建应用
```bash
./gradlew packageDistributionForCurrentOS
```

3. 运行应用
```bash
./gradlew run
```

### 预编译版本

从[Releases页面](https://github.com/your-username/windroid-toolbox/releases)下载安装包。

## 配置说明

首次运行时，请在设置中配置以下工具路径：

- **ADB路径**: Android Debug Bridge工具
- **Fastboot路径**: Fastboot工具

## 使用指南

### 1. 连接设备
- 启用开发者选项和USB调试
- 使用USB线连接设备到电脑
- 在设备上授权USB调试

### 2. 选择工具
- 在左侧工具列表中选择需要的功能
- 查看右侧详情面板中的说明

### 3. 执行操作
- 按照界面提示进行操作
- 在日志区域查看执行进度

## 安全提示

⚠️ **重要警告**：
- 刷机有风险，操作前请备份重要数据
- 确保使用正确的固件文件
- 刷机过程中请勿断开设备连接
- 本工具仅供学习和研究使用

## 开发计划

- [ ] 固件下载和管理功能
- [ ] 批量操作支持
- [ ] 插件系统
- [ ] 多语言支持

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 免责声明

本软件仅供教育和研究目的使用。使用本软件进行的任何操作所造成的设备损坏或数据丢失，开发者不承担任何责任。请在使用前仔细阅读相关文档并备份重要数据。