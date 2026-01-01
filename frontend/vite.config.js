import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // Tüm network interface'lerinden erişilebilir yapar
    port: 5173,
    strictPort: true,
    // Docker container'larından erişim için gerekli
    watch: {
      usePolling: true
    }
  }
})
