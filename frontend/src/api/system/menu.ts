import request from '@/api/index'

// 基础路径
const BASE_URL = '/system/menu'

// --- 类型定义 ---
export interface MenuQuery {
  menuName?: string
  status?: number
}

export interface MenuForm {
  id?: number
  parentId: number
  menuType: 'M' | 'C' | 'F' // M:目录 C:菜单 F:按钮
  icon?: string
  menuName: string
  sort: number
  isFrame?: number // 是否外链 (预留)
  path?: string
  component?: string
  perms?: string
  visible: number // 1显示 0隐藏
  status: number  // 1正常 0停用
}

export interface MenuTreeItem extends MenuForm {
  children?: MenuTreeItem[]
}

export interface AppRouteRecord {
  path: string
  name?: string
  component?: string
  meta?: Record<string, unknown>
  children?: AppRouteRecord[]
}

// --- 接口函数 ---

/** 获取菜单列表 (树形) */
export function listMenu(params?: MenuQuery): Promise<MenuTreeItem[]> {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

/** 新增菜单 */
export function addMenu(data: MenuForm): Promise<void> {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

/** 修改菜单 */
export function updateMenu(data: MenuForm): Promise<void> {
  return request({
    url: BASE_URL,
    method: 'put',
    data
  })
}

/** 删除菜单 */
export function delMenu(menuId: number): Promise<void> {
  return request({
    url: `${BASE_URL}/${menuId}`,
    method: 'delete'
  })
}

/** 获取动态路由 */
export function getRouters(): Promise<AppRouteRecord[]> {
  return request({
    url: '/system/menu/getRouters',
    method: 'get'
  })
}
