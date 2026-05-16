import request from '@/utils/request'

export function listRichText() {
  return request({ url: '/admin/richtext/list', method: 'get' })
}

export function getRichText(id) {
  return request({ url: `/admin/richtext/${id}`, method: 'get' })
}

export function updateRichText(id, data) {
  return request({ url: `/admin/richtext/${id}`, method: 'put', data })
}
