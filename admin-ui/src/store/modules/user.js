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
    roles: ['ROLE_DEFAULT'],
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

    // 退出系统
    logOut() {
      return new Promise((resolve, reject) => {
        logout().then(() => {
          this.token = ''
          this.roles = []
          this.permissions = []
          removeToken()
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
})

export default useUserStore
