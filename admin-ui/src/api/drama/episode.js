import request from '@/utils/request'

export function listEpisodes(videoId) {
  return request({ url: `/admin/drama/video/${videoId}/episodes`, method: 'get' })
}

export function addEpisode(videoId, data) {
  return request({ url: `/admin/drama/video/${videoId}/episodes`, method: 'post', data })
}

export function updateEpisode(epId, data) {
  return request({ url: `/admin/drama/video/episodes/${epId}`, method: 'put', data })
}

export function deleteEpisode(epId) {
  return request({ url: `/admin/drama/video/episodes/${epId}`, method: 'delete' })
}

export function transcodeEpisode(epId) {
  return request({ url: `/admin/drama/video/episodes/${epId}/transcode`, method: 'post' })
}
