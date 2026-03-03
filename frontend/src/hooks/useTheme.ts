import { ref, computed } from 'vue'

// 1. 定义主题接口
interface ThemeConfig {
  id: string
  name: string
  primary: string        // 系统主色 (按钮、选中项背景)
  menuBg: string         // 侧边栏背景
  menuText: string       // 侧边栏文字颜色
  menuActiveText: string // 侧边栏选中文字颜色 (通常等于 primary)
  logoBg: string         // Logo 区域背景 (通常比 menuBg 深一点或是同色)
}

// 2. 预设五套经典搭配
const themeList: ThemeConfig[] = [
  {
    id: 'default',
    name: '深蓝夜空 (默认)',
    primary: '#409EFF',    // 亮蓝
    menuBg: '#304156',     // 深灰蓝
    menuText: '#bfcbd9',   // 灰白文字
    menuActiveText: '#409EFF',
    logoBg: '#2b2f3a'      // 稍深的灰蓝
  },
  {
    id: 'light',
    name: '极简雅白',
    primary: '#409EFF',    // 蓝色点缀
    menuBg: '#ffffff',     // 纯白背景
    menuText: '#303133',   // 深灰文字
    menuActiveText: '#409EFF',
    logoBg: '#ffffff'      // 纯白 (带下划线区分)
  },
  {
    id: 'dark',
    name: '暗夜黑金',
    primary: '#E6A23C',    // 金色/橙色
    menuBg: '#1f1f1f',     // 纯黑灰色
    menuText: '#ffffff',   // 纯白文字
    menuActiveText: '#E6A23C',
    logoBg: '#181818'
  },
  {
    id: 'purple',
    name: '紫罗兰梦境',
    primary: '#722ED1',    // 紫色
    menuBg: '#2a0f3d',     // 深紫背景
    menuText: '#e0c2f5',   // 浅紫文字
    menuActiveText: '#ffffff', // 选中变白
    logoBg: '#1d082b'
  },
  {
    id: 'green',
    name: '森之极客',
    primary: '#11C26D',    // 绿色
    menuBg: '#222d32',     // 极客灰
    menuText: '#ffffff',
    menuActiveText: '#11C26D',
    logoBg: '#1e282c'
  }
]

// 全局状态 (单例模式，保证多组件共享)
const activeThemeId = ref('default')

export const useTheme = () => {
  // 计算当前选中的主题配置对象
  const currentTheme = computed(() => {
    return themeList.find(t => t.id === activeThemeId.value) || themeList[0]
  })

  // 简单的混色算法 (用于生成 Element Plus 的 light-1 到 light-9)
  const mix = (color1: string, color2: string, weight: number) => {
    weight = Math.max(Math.min(Number(weight), 1), 0)
    const r1 = parseInt(color1.substring(1, 3), 16)
    const g1 = parseInt(color1.substring(3, 5), 16)
    const b1 = parseInt(color1.substring(5, 7), 16)
    const r2 = parseInt(color2.substring(1, 3), 16)
    const g2 = parseInt(color2.substring(3, 5), 16)
    const b2 = parseInt(color2.substring(5, 7), 16)
    const r = Math.round(r1 * (1 - weight) + r2 * weight)
    const g = Math.round(g1 * (1 - weight) + g2 * weight)
    const b = Math.round(b1 * (1 - weight) + b2 * weight)
    const _r = ('0' + (r || 0).toString(16)).slice(-2)
    const _g = ('0' + (g || 0).toString(16)).slice(-2)
    const _b = ('0' + (b || 0).toString(16)).slice(-2)
    return '#' + _r + _g + _b
  }

  // 核心切换逻辑
  const setTheme = (themeId: string) => {
    activeThemeId.value = themeId
    const theme = themeList.find(t => t.id === themeId) || themeList[0]
    
    // 1. 设置 Element Plus 主色 (CSS 变量)
    const el = document.documentElement
    el.style.setProperty('--el-color-primary', theme.primary)
    // 生成辅色
    for (let i = 1; i <= 9; i++) {
      el.style.setProperty(`--el-color-primary-light-${i}`, mix(theme.primary, '#ffffff', i * 0.1))
    }
    el.style.setProperty(`--el-color-primary-dark-2`, mix(theme.primary, '#000000', 0.2))

    // 2. 这里我们不需要手动设置 Sidebar 的 CSS 变量，
    // 因为我们将通过 currentTheme 响应式对象直接绑定到 Sidebar 组件的 Props 上
  }

  return { 
    themeList, 
    activeThemeId, 
    currentTheme, // 导出当前主题对象给 Sidebar 使用
    setTheme 
  }
}