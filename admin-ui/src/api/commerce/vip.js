import request from '@/utils/request'

export function listVip(params) {
  return request({ url: '/admin/commerce/vip/list', method: 'get', params })
}
export function addVip(data) {
  return request({ url: '/admin/commerce/vip', method: 'post', data })
}
export function updateVip(id, data) {
  return request({ url: `/admin/commerce/vip/${id}`, method: 'put', data })
}
export function deleteVip(id) {
  return request({ url: `/admin/commerce/vip/${id}`, method: 'delete' })
}
export function listVipOrder(params) {
  return request({ url: '/admin/commerce/vip/orders', method: 'get', params })
}
export function statsVipOrder(params) {
  return request({ url: '/admin/commerce/vip/orders/stats', method: 'get', params })
}
