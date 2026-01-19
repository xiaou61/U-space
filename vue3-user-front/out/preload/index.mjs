import { contextBridge, ipcRenderer } from "electron";
contextBridge.exposeInMainWorld("electronAPI", {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.invoke("window:minimize"),
  maximizeWindow: () => ipcRenderer.invoke("window:maximize"),
  closeWindow: () => ipcRenderer.invoke("window:close"),
  isMaximized: () => ipcRenderer.invoke("window:isMaximized"),
  // 系统功能
  openExternal: (url) => ipcRenderer.invoke("shell:openExternal", url),
  // 应用信息
  getVersion: () => ipcRenderer.invoke("app:getVersion"),
  // 设置
  getSettings: () => ipcRenderer.invoke("settings:get"),
  setSettings: (settings) => ipcRenderer.invoke("settings:set", settings),
  // 平台信息
  platform: process.platform,
  // 判断是否在 Electron 环境中
  isElectron: true
});
