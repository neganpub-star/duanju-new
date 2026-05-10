import request from '@/utils/request'

export function listDramaUser(params) {
  return request({ url: '/admin/system/user/list', method: 'get', params })
}
export function getDramaUser(id) {
  return request({ url: `/admin/system/user/${id}`, method: 'get' })
}
export function rechargeUser(id, data) {
  return request({ url: `/admin/system/user/${id}/recharge`, method: 'post', data })
}
