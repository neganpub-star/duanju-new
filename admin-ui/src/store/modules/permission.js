import router, { constantRoutes } from '@/router'

const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  }),
  actions: {
    setRoutes(routes) {
      this.addRoutes = routes
      this.routes = constantRoutes.concat(routes)
    },
    // 使用静态路由，不请求后端菜单接口
    generateRoutes() {
      return new Promise(resolve => {
        this.setRoutes([])
        this.sidebarRouters = constantRoutes
        this.defaultRoutes = constantRoutes
        this.topbarRouters = constantRoutes
        resolve([])
      })
    }
  }
})

export default usePermissionStore
