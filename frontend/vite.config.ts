import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
//组件中自动添加组件名
import vueSetupExtend from 'vite-plugin-vue-setup-extend'

export default defineConfig({
  plugins: [vue(), vueSetupExtend()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 配置source map，用于调试
  // build: {
  //   sourcemap: true
  // },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // 代理配置：将 /api 开头的请求转发到后端 8080 端口
      '/api': {
        target: 'http://localhost:29020',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        ws: true
      }
    },
    hmr: {
      overlay: false
    }
  }
})