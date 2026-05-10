import request from '@/utils/request'

// 管理员登录
export function login(username, password) {
  return request({
    url: '/admin/auth/login',
    headers: { isToken: false, repeatSubmit: false },
    method: 'post',
    data: { username, password }
  })
}

// 获取当前管理员信息
export function getInfo() {
  return request({
    url: '/admin/auth/info',
    method: 'get'
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

// 锁屏解锁（无需后端接口，前端本地验证）
export function unlockScreen() {
  return Promise.resolve({})
}

// 注册（后管不开放注册）
export function register() {
  return Promise.reject(new Error('后管不支持注册'))
}

// 验证码（后管不启用验证码）
export function getCodeImg() {
  return Promise.resolve({ captchaEnabled: false })
}
