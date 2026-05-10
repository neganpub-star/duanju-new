import request from '@/utils/request'

export function listVideo(params) {
  return request({ url: '/admin/drama/video/list', method: 'get', params })
}

export function getVideo(id) {
  return request({ url: `/admin/drama/video/${id}`, method: 'get' })
}

export function addVideo(data) {
  return request({ url: '/admin/drama/video', method: 'post', data })
}

export function updateVideo(id, data) {
  return request({ url: `/admin/drama/video/${id}`, method: 'put', data })
}

export function deleteVideo(id) {
  return request({ url: `/admin/drama/video/${id}`, method: 'delete' })
}

export function listEpisodes(videoId) {
  return request({ url: `/admin/drama/video/${videoId}/episodes`, method: 'get' })
}

export function saveEpisodes(videoId, data) {
  return request({ url: `/admin/drama/video/${videoId}/episodes`, method: 'post', data })
}
