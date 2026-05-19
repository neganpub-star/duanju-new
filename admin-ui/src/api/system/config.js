import request from '@/utils/request'

// RuoYi 全局方法依赖，保留 stub 避免 import 报错
export function getConfigKey(configKey) {
  return Promise.resolve({ msg: '' })
}

export function listConfig() {
  return request({ url: '/admin/system/config/list', method: 'get' })
}

export function batchUpdateConfig(data) {
  return request({ url: '/admin/system/config/batch', method: 'put', data })
}

export function addConfig(data) {
  return request({ url: '/admin/system/config', method: 'post', data })
}

export function deleteConfig(key) {
  return request({ url: `/admin/system/config/${encodeURIComponent(key)}`, method: 'delete' })
}

export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData,
    timeout: 0,
    onUploadProgress: (e) => {
      if (typeof onProgress === 'function' && e && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    }
  })
}

/**
 * 远程 URL 入库：本平台 OSS 直接返回；外部 URL 后端下载并转存到 OSS，返回新 URL
 */
export function fetchRemoteUrl(url) {
  return request({
    url: '/admin/upload/remote',
    method: 'post',
    data: { url },
    timeout: 0
  })
}
