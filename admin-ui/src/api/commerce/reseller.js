import request from '@/utils/request'

export function listReseller(params) {
  return request({ url: '/admin/commerce/reseller/list', method: 'get', params })
}
export function addReseller(data) {
  return request({ url: '/admin/commerce/reseller', method: 'post', data })
}
export function updateReseller(id, data) {
  return request({ url: `/admin/commerce/reseller/${id}`, method: 'put', data })
}
export function deleteReseller(id) {
  return request({ url: `/admin/commerce/reseller/${id}`, method: 'delete' })
}
export function listResellerOrder(params) {
  return request({ url: '/admin/commerce/reseller/orders', method: 'get', params })
}
