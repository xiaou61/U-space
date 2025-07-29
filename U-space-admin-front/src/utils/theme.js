export const THEME_KEY = 'app-theme'

export const setTheme = (mode) => {
  const root = document.documentElement
  if(mode === 'dark'){
    root.classList.add('dark')
  }else{
    root.classList.remove('dark')
  }
  localStorage.setItem(THEME_KEY, mode)
}

export const initTheme = () => {
  const saved = localStorage.getItem(THEME_KEY) || 'light'
  setTheme(saved)
  return saved
}

export const toggleTheme = () => {
  const current = document.documentElement.classList.contains('dark') ? 'dark' : 'light'
  setTheme(current === 'dark' ? 'light' : 'dark')
} 