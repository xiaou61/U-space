import require$$0, { BrowserWindow, shell, nativeImage, Tray, Menu, app, ipcMain } from "electron";
import { join } from "path";
import Store from "electron-store";
import __cjs_url__ from "node:url";
import __cjs_path__ from "node:path";
import __cjs_mod__ from "node:module";
const __filename = __cjs_url__.fileURLToPath(import.meta.url);
const __dirname = __cjs_path__.dirname(__filename);
const require2 = __cjs_mod__.createRequire(import.meta.url);
var dist = {};
Object.defineProperty(dist, "__esModule", { value: true });
var electron = require$$0;
const is = {
  dev: !electron.app.isPackaged
};
const platform = {
  isWindows: process.platform === "win32",
  isMacOS: process.platform === "darwin",
  isLinux: process.platform === "linux"
};
const electronApp = {
  setAppUserModelId(id) {
    if (platform.isWindows)
      electron.app.setAppUserModelId(is.dev ? process.execPath : id);
  },
  setAutoLaunch(auto) {
    if (platform.isLinux)
      return false;
    const isOpenAtLogin = () => {
      return electron.app.getLoginItemSettings().openAtLogin;
    };
    if (isOpenAtLogin() !== auto) {
      electron.app.setLoginItemSettings({
        openAtLogin: auto,
        path: process.execPath
      });
      return isOpenAtLogin() === auto;
    } else {
      return true;
    }
  },
  skipProxy() {
    return electron.session.defaultSession.setProxy({ mode: "direct" });
  }
};
let listeners = [];
let handlers = [];
const ipcHelper = {
  handle(channel, listener) {
    handlers.push(channel);
    electron.ipcMain.handle(channel, listener);
  },
  on(channel, listener) {
    listeners.push(channel);
    electron.ipcMain.on(channel, listener);
    return this;
  },
  removeAllListeners() {
    listeners.forEach((c) => electron.ipcMain.removeAllListeners(c));
    listeners = [];
    return this;
  },
  removeAllHandlers() {
    handlers.forEach((c) => electron.ipcMain.removeHandler(c));
    handlers = [];
  },
  removeListeners(channels) {
    channels.forEach((c) => electron.ipcMain.removeAllListeners(c));
    return this;
  },
  removeHandlers(channels) {
    channels.forEach((c) => electron.ipcMain.removeHandler(c));
  }
};
const optimizer = {
  watchWindowShortcuts(window, shortcutOptions) {
    if (!window)
      return;
    const { webContents } = window;
    const { escToCloseWindow = false, zoom = false } = shortcutOptions || {};
    webContents.on("before-input-event", (event, input) => {
      if (input.type === "keyDown") {
        if (!is.dev) {
          if (input.key === "r" && (input.control || input.meta))
            event.preventDefault();
        } else {
          if (input.code === "F12") {
            if (webContents.isDevToolsOpened()) {
              webContents.closeDevTools();
            } else {
              webContents.openDevTools({ mode: "undocked" });
              console.log("Open dev tool...");
            }
          }
        }
        if (escToCloseWindow) {
          if (input.code === "Escape" && input.key !== "Process") {
            window.close();
            event.preventDefault();
          }
        }
        if (!zoom) {
          if (input.code === "Minus" && (input.control || input.meta))
            event.preventDefault();
          if (input.code === "Equal" && input.shift && (input.control || input.meta))
            event.preventDefault();
        }
      }
    });
  },
  registerFramelessWindowIpc() {
    electron.ipcMain.on("win:invoke", (event, action) => {
      const win = electron.BrowserWindow.fromWebContents(event.sender);
      if (win) {
        if (action === "show") {
          win.show();
        } else if (action === "showInactive") {
          win.showInactive();
        } else if (action === "min") {
          win.minimize();
        } else if (action === "max") {
          const isMaximized = win.isMaximized();
          if (isMaximized) {
            win.unmaximize();
          } else {
            win.maximize();
          }
        } else if (action === "close") {
          win.close();
        }
      }
    });
  }
};
dist.electronApp = electronApp;
dist.ipcHelper = ipcHelper;
var is_1 = dist.is = is;
dist.optimizer = optimizer;
dist.platform = platform;
let store = null;
function initStore() {
  store = new Store({
    defaults: {
      windowBounds: {
        width: 1400,
        height: 900
      },
      minimizeToTray: true,
      autoStart: false
    }
  });
}
function getStore() {
  return store;
}
let mainWindow = null;
function createWindow() {
  const store2 = getStore();
  const bounds = store2?.get("windowBounds", {
    width: 1400,
    height: 900
  });
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
    icon: join(__dirname, "../../electron/resources/icon.png"),
    webPreferences: {
      preload: join(__dirname, "../preload/index.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false
    }
  });
  mainWindow.on("ready-to-show", () => {
    mainWindow?.show();
  });
  mainWindow.on("close", () => {
    if (mainWindow && !mainWindow.isDestroyed()) {
      store2?.set("windowBounds", mainWindow.getBounds());
    }
  });
  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url);
    return { action: "deny" };
  });
  if (is_1.dev && process.env["ELECTRON_RENDERER_URL"]) {
    mainWindow.loadURL(process.env["ELECTRON_RENDERER_URL"]);
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(join(__dirname, "../renderer/index.html"));
  }
  return mainWindow;
}
function getMainWindow() {
  return mainWindow;
}
function showWindow() {
  if (mainWindow) {
    if (mainWindow.isMinimized()) {
      mainWindow.restore();
    }
    mainWindow.show();
    mainWindow.focus();
  }
}
let tray = null;
function createTray() {
  const iconPath = join(__dirname, "../../electron/resources/icon.png");
  const icon = nativeImage.createFromPath(iconPath);
  tray = new Tray(icon.resize({ width: 16, height: 16 }));
  const contextMenu = Menu.buildFromTemplate([
    {
      label: "打开 Code-Nest",
      click: () => {
        showWindow();
      }
    },
    {
      type: "separator"
    },
    {
      label: "关于",
      click: () => {
        const { dialog } = require2("electron");
        dialog.showMessageBox({
          type: "info",
          title: "关于 Code-Nest",
          message: "Code-Nest 用户端",
          detail: `版本: ${app.getVersion()}
基于 Electron 构建`
        });
      }
    },
    {
      type: "separator"
    },
    {
      label: "退出",
      click: () => {
        app.quit();
      }
    }
  ]);
  tray.setToolTip("Code-Nest");
  tray.setContextMenu(contextMenu);
  tray.on("double-click", () => {
    showWindow();
  });
}
const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) {
  app.quit();
} else {
  app.on("second-instance", () => {
    const mainWindow2 = getMainWindow();
    if (mainWindow2) {
      if (mainWindow2.isMinimized()) mainWindow2.restore();
      mainWindow2.focus();
    }
  });
  app.whenReady().then(() => {
    initStore();
    createWindow();
    createTray();
    setupIPC();
  });
}
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
function setupIPC() {
  ipcMain.handle("window:minimize", () => {
    const win = getMainWindow();
    win?.minimize();
  });
  ipcMain.handle("window:maximize", () => {
    const win = getMainWindow();
    if (win?.isMaximized()) {
      win.unmaximize();
    } else {
      win?.maximize();
    }
  });
  ipcMain.handle("window:close", () => {
    const store2 = getStore();
    const minimizeToTray = store2?.get("minimizeToTray", true);
    const win = getMainWindow();
    if (minimizeToTray) {
      win?.hide();
    } else {
      win?.close();
    }
  });
  ipcMain.handle("window:isMaximized", () => {
    return getMainWindow()?.isMaximized();
  });
  ipcMain.handle("shell:openExternal", async (_event, url) => {
    await shell.openExternal(url);
  });
  ipcMain.handle("app:getVersion", () => {
    return app.getVersion();
  });
  ipcMain.handle("settings:get", () => {
    const store2 = getStore();
    return {
      minimizeToTray: store2?.get("minimizeToTray", true),
      autoStart: store2?.get("autoStart", false)
    };
  });
  ipcMain.handle("settings:set", (_event, settings) => {
    const store2 = getStore();
    Object.entries(settings).forEach(([key, value]) => {
      store2?.set(key, value);
    });
    if ("autoStart" in settings) {
      app.setLoginItemSettings({
        openAtLogin: settings.autoStart
      });
    }
  });
}
