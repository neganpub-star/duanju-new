import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isEmpty } from '@/utils/validate'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    id: '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  }),
  actions: {
    // 登录 — 对接 /admin/auth/login，返回 {token, adminId, username, nickname, avatar}
    login(userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      return new Promise((resolve, reject) => {
        login(username, password).then(res => {
          setToken(res.data.token)
          this.token = res.data.token
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取管理员信息 — 对接 /admin/auth/info，返回 SysAdmin 对象
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const admin = res.data
          const avatarUrl = isEmpty(admin.avatar) ? defAva : admin.avatar
          this.id = admin.id
          this.name = admin.username
          this.nickName = admin.nickname || admin.username
          this.avatar = avatarUrl
          this.roles = ['ROLE_DEFAULT']
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出系统 — 无论后端是否响应成功，都清除本地状态
    logOut() {
      this.token = ''
      this.roles = []
      this.permissions = []
      removeToken()
      logout().catch(() => {}) // 忽略后端失败（token 已过期时正常）
      return Promise.resolve()
    }
  }
})

export default useUserStore
