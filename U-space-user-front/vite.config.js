import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
    // If you prefer using a proxy instead of CORS from backend, uncomment below
    proxy: {
      '/uapi': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/uapi/, '/uapi'),
      },
    },
  },
}) 