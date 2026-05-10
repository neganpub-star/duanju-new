import request from '@/utils/request'

export function listCategory(params) {
  return request({ url: '/admin/drama/category/list', method: 'get', params })
}

export function addCategory(data) {
  return request({ url: '/admin/drama/category', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/admin/drama/category/${id}`, method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/admin/drama/category/${id}`, method: 'delete' })
}
