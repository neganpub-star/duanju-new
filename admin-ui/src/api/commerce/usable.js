import request from '@/utils/request'

export function listUsable(params) {
  return request({ url: '/admin/commerce/usable/list', method: 'get', params })
}
export function addUsable(data) {
  return request({ url: '/admin/commerce/usable', method: 'post', data })
}
export function updateUsable(id, data) {
  return request({ url: `/admin/commerce/usable/${id}`, method: 'put', data })
}
export function deleteUsable(id) {
  return request({ url: `/admin/commerce/usable/${id}`, method: 'delete' })
}
