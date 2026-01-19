import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.invoke('window:minimize'),
  maximizeWindow: () => ipcRenderer.invoke('window:maximize'),
  closeWindow: () => ipcRenderer.invoke('window:close'),
  isMaximized: () => ipcRenderer.invoke('window:isMaximized'),

  // 系统功能
  openExternal: (url: string) => ipcRenderer.invoke('shell:openExternal', url),

  // 应用信息
  getVersion: () => ipcRenderer.invoke('app:getVersion'),

  // 设置
  getSettings: () => ipcRenderer.invoke('settings:get'),
  setSettings: (settings: Record<string, any>) => ipcRenderer.invoke('settings:set', settings),

  // 平台信息
  platform: process.platform,

  // 判断是否在 Electron 环境中
  isElectron: true
})

export interface ElectronAPI {
  minimizeWindow: () => Promise<void>
  maximizeWindow: () => Promise<void>
  closeWindow: () => Promise<void>
  isMaximized: () => Promise<boolean>
  openExternal: (url: string) => Promise<void>
  getVersion: () => Promise<string>
  getSettings: () => Promise<{ minimizeToTray: boolean; autoStart: boolean }>
  setSettings: (settings: Record<string, any>) => Promise<void>
  platform: NodeJS.Platform
  isElectron: boolean
}

declare global {
  interface Window {
    electronAPI: ElectronAPI
  }
}
