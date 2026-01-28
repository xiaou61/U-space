import { BrowserWindow, shell } from 'electron'
import { join } from 'path'
import { is } from '@electron-toolkit/utils'
import { getStore } from './store'

let mainWindow: BrowserWindow | null = null

export function createWindow(): BrowserWindow {
  const store = getStore()
  
  // 获取上次保存的窗口位置和大小
  const bounds = store?.get('windowBounds', {
    width: 1400,
    height: 900
  }) as { width: number; height: number; x?: number; y?: number }

  mainWindow = new BrowserWindow({
    width: bounds.width,
    height: bounds.height,
    x: bounds.x,
    y: bounds.y,
    minWidth: 1200,
    minHeight: 800,
    show: false,
    frame: true,
    autoHideMenuBar: true,
    icon: join(__dirname, '../../electron/resources/icon.png'),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false
    }
  })

  // 窗口准备好后显示
  mainWindow.on('ready-to-show', () => {
    mainWindow?.show()
  })

  // 保存窗口状态
  mainWindow.on('close', () => {
    if (mainWindow && !mainWindow.isDestroyed()) {
      store?.set('windowBounds', mainWindow.getBounds())
    }
  })

  // 在默认浏览器中打开外部链接
  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // 开发环境加载 dev server，生产环境加载打包文件
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
    // 开发环境下打开 DevTools
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }

  return mainWindow
}

export function getMainWindow(): BrowserWindow | null {
  return mainWindow
}

export function showWindow(): void {
  if (mainWindow) {
    if (mainWindow.isMinimized()) {
      mainWindow.restore()
    }
    mainWindow.show()
    mainWindow.focus()
  }
}

export function hideWindow(): void {
  mainWindow?.hide()
}
