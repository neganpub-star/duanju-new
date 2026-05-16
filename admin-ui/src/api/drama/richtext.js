import request from '@/utils/request'

export function listDocKeys() {
  return request({ url: '/admin/richtext/doc-keys', method: 'get' })
}

export function listRichText(lang = 'zh-CN') {
  return request({ url: '/admin/richtext/list', method: 'get', params: { lang } })
}

export function getByDocKey(docKey, lang) {
  return request({ url: `/admin/richtext/doc/${docKey}`, method: 'get', params: { lang } })
}

export function saveByDocKey(docKey, lang, data) {
  return request({ url: `/admin/richtext/doc/${docKey}`, method: 'put', params: { lang }, data })
}

export function getRichText(id) {
  return request({ url: `/admin/richtext/${id}`, method: 'get' })
}

export function updateRichText(id, data) {
  return request({ url: `/admin/richtext/${id}`, method: 'put', data })
}
