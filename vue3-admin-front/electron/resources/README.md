# 应用图标资源

请在此目录放置应用图标文件：

## 必需文件

- `icon.ico` - Windows 图标 (256x256 或更大，包含多个尺寸)
- `icon.png` - 通用 PNG 图标 (512x512 推荐)

## 可选文件

- `icon.icns` - macOS 图标
- `tray.png` - 系统托盘图标 (16x16 或 32x32)

## 图标制作建议

1. 使用 1024x1024 的源图像
2. 使用在线工具生成多平台图标：
   - https://www.icoconverter.com/ (生成 .ico)
   - https://cloudconvert.com/ (转换格式)
   - https://iconifier.net/ (生成 macOS .icns)

## 临时方案

开发阶段可以使用简单的占位图标，打包前替换为正式图标。
