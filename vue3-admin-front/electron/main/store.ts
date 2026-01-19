import Store from 'electron-store'

interface StoreSchema {
  windowBounds: {
    width: number
    height: number
    x?: number
    y?: number
  }
  minimizeToTray: boolean
  autoStart: boolean
}

let store: Store<StoreSchema> | null = null

export function initStore(): void {
  store = new Store<StoreSchema>({
    defaults: {
      windowBounds: {
        width: 1400,
        height: 900
      },
      minimizeToTray: true,
      autoStart: false
    }
  })
}

export function getStore(): Store<StoreSchema> | null {
  return store
}
